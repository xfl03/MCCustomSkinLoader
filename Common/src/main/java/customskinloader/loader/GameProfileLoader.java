package customskinloader.loader;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;
import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.TextureUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

public class GameProfileLoader implements ICustomSkinLoaderPlugin, ProfileLoader.IProfileLoader {
    @Override
    public ProfileLoader.IProfileLoader getProfileLoader() {
        return this;
    }

    @Override
    public List<IDefaultProfile> getDefaultProfiles() {
        return Lists.newArrayList(new ServerGameProfile(this));
    }

    public static class ServerGameProfile implements ICustomSkinLoaderPlugin.IDefaultProfile {
        protected final GameProfileLoader loader;

        public ServerGameProfile(GameProfileLoader loader) {
            this.loader = loader;
        }

        @Override
        public String getName() {
            return "GameProfile";
        }

        @Override
        public int getPriority() {
            return 50;
        }

        @Override
        public void updateSkinSiteProfile(SkinSiteProfile ssp) {
            ssp.type = this.loader.getName();
        }
    }

    @Override
    public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = getTextures(TextureUtil.AuthlibField.GAME_PROFILE_PROPERTIES.get(gameProfile));
        if (!map.isEmpty()) {
            CustomSkinLoader.logger.info("Default profile will be used.");
            return ModelManager0.toUserProfile(map);
        }
        CustomSkinLoader.logger.info("Profile not found.");
        return null;
    }

    @Override
    public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
        return true;
    }

    @Override
    public String getName() {
        return "GameProfile";
    }

    @Override
    public void init(SkinSiteProfile ssp) {

    }

    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create();
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(PropertyMap map) {
        Property textureProperty = Iterables.getFirst(map.get("textures"), null);
        if (textureProperty == null) {
            return Maps.newHashMap();
        }
        String value = TextureUtil.AuthlibField.PROPERTY_VALUE.get(textureProperty);
        if (StringUtils.isBlank(value)) {
            return Maps.newHashMap();
        }
        String json = new String(Base64.decodeBase64(value), StandardCharsets.UTF_8);
        MinecraftTexturesPayload result = GSON.fromJson(json, MinecraftTexturesPayload.class);

        if (result == null) {
            return Maps.newHashMap();
        }
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = TextureUtil.AuthlibField.MINECRAFT_TEXTURES_PAYLOAD_TEXTURES.get(result);
        if (textures == null) {
            return Maps.newHashMap();
        }
        return textures;
    }
}
