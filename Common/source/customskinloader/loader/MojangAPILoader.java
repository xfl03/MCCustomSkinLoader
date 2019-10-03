package customskinloader.loader;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpRequestUtil.HttpRequest;
import customskinloader.utils.HttpRequestUtil.HttpResponce;

public class MojangAPILoader implements ProfileLoader.IProfileLoader {

    @Override
    public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = getTextures(gameProfile);
        if (!map.isEmpty()) {
            CustomSkinLoader.logger.info("Default profile will be used.");
            return ModelManager0.toUserProfile(map);
        }
        String username = gameProfile.getName();
        GameProfile newGameProfile = loadGameProfile(ssp.apiRoot, username);
        if (newGameProfile == null) {
            CustomSkinLoader.logger.info("Profile not found.(" + username + "'s profile not found.)");
            return null;
        }
        newGameProfile = fillGameProfile(ssp.sessionRoot, newGameProfile);
        map = getTextures(newGameProfile);
        if (!map.isEmpty()) {
            gameProfile.getProperties().putAll(newGameProfile.getProperties());
            return ModelManager0.toUserProfile(map);
        }
        CustomSkinLoader.logger.info("Profile not found.(" + username + " doesn't have skin/cape.)");
        return null;
    }

    //Username -> UUID
    public static GameProfile loadGameProfile(String apiRoot, String username) {
        //Doc (https://wiki.vg/Mojang_API#Playernames_-.3E_UUIDs)
        Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

        HttpResponce responce = HttpRequestUtil.makeHttpRequest(
                new HttpRequest(apiRoot + "profiles/minecraft")
                        .setCacheTime(-1).setPayload(gson.toJson(Collections.singletonList(username)))
        );
        if (StringUtils.isEmpty(responce.content))
            return null;


        GameProfile[] profiles = gson.fromJson(responce.content, GameProfile[].class);
        if (profiles.length == 0) return null;
        GameProfile gameProfile = profiles[0];

        if (gameProfile.getId() == null)
            return null;
        return new GameProfile(gameProfile.getId(), gameProfile.getName());
    }

    //UUID -> Profile
    public static GameProfile fillGameProfile(String sessionRoot, GameProfile profile) {
        //Doc (http://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape)
        HttpResponce responce = HttpRequestUtil.makeHttpRequest(
                new HttpRequest(sessionRoot + "session/minecraft/profile/"
                        + UUIDTypeAdapter.fromUUID(profile.getId())).setCacheTime(90));
        if (StringUtils.isEmpty(responce.content))
            return profile;

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
                .registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer())
                .create();
        MinecraftProfilePropertiesResponse propertiesResponce = gson.fromJson(responce.content, MinecraftProfilePropertiesResponse.class);
        GameProfile newGameProfile = new GameProfile(propertiesResponce.getId(), propertiesResponce.getName());
        newGameProfile.getProperties().putAll(propertiesResponce.getProperties());

        return newGameProfile;
    }

    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile gameProfile) {
        if (gameProfile == null)
            return Maps.newHashMap();
        Property textureProperty = Iterables.getFirst(gameProfile.getProperties().get("textures"), null);
        if (textureProperty == null)
            return Maps.newHashMap();
        String value = textureProperty.getValue();
        if (StringUtils.isBlank(value))
            return Maps.newHashMap();
        @SuppressWarnings("deprecation") String json = new String(Base64.decodeBase64(value), Charsets.UTF_8);
        Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
        MinecraftTexturesPayload result = gson.fromJson(json, MinecraftTexturesPayload.class);
        if (result == null || result.getTextures() == null)
            return Maps.newHashMap();
        return result.getTextures();
    }

    @Override
    public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
        return true;
    }

    @Override
    public String getName() {
        return "MojangAPI";
    }

    @Override
    public void init(SkinSiteProfile ssp) {
        //Init default api & session root for Mojang API
        if (ssp.apiRoot == null)
            ssp.apiRoot = "https://api.mojang.com/";
        if (ssp.sessionRoot == null)
            ssp.sessionRoot = "https://sessionserver.mojang.com/";
    }

    private static final String MOJANG_API_ROOT = "https://api{DO_NOT_MODIFY}.mojang.com/";

    public static String getMojangApiRoot() {
        return MOJANG_API_ROOT.replace("{DO_NOT_MODIFY}", "");
    }
}
