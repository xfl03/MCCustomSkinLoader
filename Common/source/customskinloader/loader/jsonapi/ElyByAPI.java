package customskinloader.loader.jsonapi;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;

public class ElyByAPI implements JsonAPILoader.IJsonAPI {

    public static class ElyBy extends JsonAPILoader.DefaultProfile {
        public ElyBy(JsonAPILoader loader) { super(loader); }
        @Override public String getName()  { return "ElyBy"; }
        @Override public int getPriority() { return 400; }
        @Override public String getRoot()  { return "http://skinsystem.ely.by/textures/"; }
    }

    @Override
    public List<ICustomSkinLoaderPlugin.IDefaultProfile> getDefaultProfiles(JsonAPILoader loader) {
        return Lists.newArrayList(new ElyBy(loader));
    }

    @Override
    public String toJsonUrl(String root, String username) {
        return root + username;
    }

    @Override
    public UserProfile toUserProfile(String root, String json, boolean local) {
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> result = new Gson().fromJson(json, new TypeToken<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>() { }.getType());
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
}
