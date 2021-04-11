package customskinloader.loader.jsonapi;

import java.net.URI;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;

public class ElyByAPI implements JsonAPILoader.IJsonAPI {
    @Override
    public String toJsonUrl(String root, String username) {
        return root + username;
    }

    @Override
    public UserProfile toUserProfile(String root, String json, boolean local) {
        Map<Type, MinecraftProfileTexture> result = new Gson().fromJson(json, new TypeToken<Map<Type, MinecraftProfileTexture>>() { }.getType());
        return ModelManager0.toUserProfile(result);
    }

    @Override
    public String getPayload(SkinSiteProfile ssp) {
        return null;
    }

    @Override
    public String getName() {
        return "ElyByAPI";
    }

    @Override
    public boolean checkRoot() {
        return false;
    }
}
