package customskinloader.loader;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.TextureUtil;
import org.apache.commons.lang3.StringUtils;

public class MojangAPILoader implements ICustomSkinLoaderPlugin, ProfileLoader.IProfileLoader {

    @Override
    public ProfileLoader.IProfileLoader getProfileLoader() {
        return this;
    }

    @Override
    public List<IDefaultProfile> getDefaultProfiles() {
        return Lists.newArrayList(new Mojang(this));
    }

    public abstract static class DefaultProfile implements ICustomSkinLoaderPlugin.IDefaultProfile {
        protected final MojangAPILoader loader;

        public DefaultProfile(MojangAPILoader loader) {
            this.loader = loader;
        }

        @Override
        public void updateSkinSiteProfile(SkinSiteProfile ssp) {
            ssp.type = this.loader.getName();
            ssp.apiRoot = this.getAPIRoot();
            ssp.sessionRoot = this.getSessionRoot();
        }

        public abstract String getAPIRoot();

        public abstract String getSessionRoot();
    }

    public static class Mojang extends MojangAPILoader.DefaultProfile {
        public Mojang(MojangAPILoader loader) {
            super(loader);
        }

        @Override
        public String getName() {
            return "Mojang";
        }

        @Override
        public int getPriority() {
            return 100;
        }

        @Override
        public String getAPIRoot() {
            return getMojangApiRoot();
        }

        @Override
        public String getSessionRoot() {
            return getMojangSessionRoot();
        }
    }

    @Override
    public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) {
        String username = TextureUtil.AuthlibField.GAME_PROFILE_NAME.get(gameProfile);
        MinecraftProfilePropertiesResponse newProfile = loadGameProfileCached(ssp.apiRoot, username);
        if (newProfile == null) {
            CustomSkinLoader.logger.info("Profile not found.(" + username + "'s profile not found.)");
            return null;
        }
        PropertyMap propertyMap = TextureUtil.AuthlibField.MINECRAFT_PROFILE_PROPERTIES_RESPONSE_PROPERTIES.get(fillProfile(ssp.sessionRoot, newProfile));
        if (propertyMap != null) {
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = GameProfileLoader.getTextures(propertyMap);
            if (!map.isEmpty()) {
                return ModelManager0.toUserProfile(map);
            }
        }
        CustomSkinLoader.logger.info("Profile not found.(" + username + " doesn't have skin/cape.)");
        return null;
    }

    private static final Map<String, MinecraftProfilePropertiesResponse> gameProfileCache = new ConcurrentHashMap<>();

    public static MinecraftProfilePropertiesResponse loadGameProfileCached(String apiRoot, String username) {
        return gameProfileCache.computeIfAbsent(apiRoot + " " + username, ignored -> loadGameProfile(apiRoot, username));
    }

    //Username -> UUID
    public static MinecraftProfilePropertiesResponse loadGameProfile(String apiRoot, String username) {
        //Doc (https://minecraft.wiki/w/Mojang_API#Query_player_UUIDs_in_batch)
        HttpRequestUtil.HttpResponce responce = HttpRequestUtil.makeHttpRequest(new HttpRequestUtil.HttpRequest(apiRoot + "profiles/minecraft").setCacheTime(600).setPayload(GameProfileLoader.GSON.toJson(Collections.singletonList(username))));
        if (StringUtils.isEmpty(responce.content)) {
            return null;
        }

        MinecraftProfilePropertiesResponse[] profiles = GameProfileLoader.GSON.fromJson(responce.content, MinecraftProfilePropertiesResponse[].class);
        if (profiles.length == 0) {
            return null;
        }

        UUID id = TextureUtil.AuthlibField.MINECRAFT_PROFILE_PROPERTIES_RESPONSE_ID.get(profiles[0]);
        if (id == null) {
            return null;
        }
        return profiles[0];
    }

    /**
     * Get Mojang UUID by username.
     *
     * @param username username to query
     * @param standard if - in UUID
     * @return UUID in Mojang API style string. Returns {@code null} if username not found in Mojang API.
     */
    public static String getMojangUuidByUsername(String username, boolean standard) {
        MinecraftProfilePropertiesResponse profile = loadGameProfileCached(getMojangApiRoot(), username);
        if (profile == null) {
            return null;
        }
        UUID id = TextureUtil.AuthlibField.MINECRAFT_PROFILE_PROPERTIES_RESPONSE_ID.get(profile);
        return standard ? id.toString() : TextureUtil.fromUUID(id);
    }

    //UUID -> Profile
    public static MinecraftProfilePropertiesResponse fillProfile(String sessionRoot, MinecraftProfilePropertiesResponse profile) {
        //Doc (https://minecraft.wiki/w/Mojang_API#Query_player's_skin_and_cape)
        HttpRequestUtil.HttpResponce responce = HttpRequestUtil.makeHttpRequest(new HttpRequestUtil.HttpRequest(sessionRoot + "session/minecraft/profile/" + TextureUtil.fromUUID(TextureUtil.AuthlibField.MINECRAFT_PROFILE_PROPERTIES_RESPONSE_ID.get(profile))).setCacheTime(90));
        if (StringUtils.isEmpty(responce.content)) {
            return profile;
        }
        return GameProfileLoader.GSON.fromJson(responce.content, MinecraftProfilePropertiesResponse.class);
    }

    @Override
    public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
        return (!StringUtils.isNoneEmpty(ssp0.apiRoot) || ssp0.apiRoot.equalsIgnoreCase(ssp1.apiRoot)) || (!StringUtils.isNoneEmpty(ssp0.sessionRoot) || ssp0.sessionRoot.equalsIgnoreCase(ssp1.sessionRoot));
    }

    @Override
    public String getName() {
        return "MojangAPI";
    }

    @Override
    public void init(SkinSiteProfile ssp) {
        //Init default api & session root for Mojang API
        if (ssp.apiRoot == null)
            ssp.apiRoot = getMojangApiRoot();
        if (ssp.sessionRoot == null)
            ssp.sessionRoot = getMojangSessionRoot();
    }

    // Prevent authlib-injector (https://github.com/yushijinhun/authlib-injector) from modifying these strings
    private static final String MOJANG_API_ROOT = "https://api{DO_NOT_MODIFY}.mojang.com/";
    private static final String MOJANG_SESSION_ROOT = "https://sessionserver{DO_NOT_MODIFY}.mojang.com/";

    public static String getMojangApiRoot() {
        return MOJANG_API_ROOT.replace("{DO_NOT_MODIFY}", "");
    }

    public static String getMojangSessionRoot() {
        return MOJANG_SESSION_ROOT.replace("{DO_NOT_MODIFY}", "");
    }
}
