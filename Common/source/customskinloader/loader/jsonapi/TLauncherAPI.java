package customskinloader.loader.jsonapi;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;

import java.util.List;
import java.util.Map;

public class TLauncherAPI implements JsonAPILoader.IJsonAPI {
    public static class TLauncher extends JsonAPILoader.DefaultProfile {
        public TLauncher(JsonAPILoader loader) { super(loader); }
        @Override public String getName()   { return "TLauncher"; }
        @Override public int getPriority()  { return 900; }
        @Override public String getRoot()   { return "https://auth.tlauncher.org/skin/profile/texture/login/"; }
    }

    @Override
    public List<ICustomSkinLoaderPlugin.IDefaultProfile> getDefaultProfiles(JsonAPILoader loader) {
        return Lists.newArrayList(new TLauncher(loader));
    }

    @Override
    public String toJsonUrl(String root, String username) {
        return root + username;
    }

    @Override
    public String getPayload(SkinSiteProfile ssp) {
        return null;
    }

    @Override
    public UserProfile toUserProfile(String root, String json, boolean local) {
        TLauncherAPIResponse res = new Gson().fromJson(json, TLauncherAPIResponse.class);
        if(!res.textures.containsKey(MinecraftProfileTexture.Type.SKIN)) return null;

        return ModelManager0.toUserProfile(res.textures);
    }

    public static class TLauncherAPIResponse {
        protected Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures;
    }

    @Override
    public String getName() {
        return "TLauncherAPI";
    }
}
