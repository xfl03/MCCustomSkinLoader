package customskinloader.loader.jsonapi;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;

public class GlitchlessAPI implements JsonAPILoader.IJsonAPI {

    public static class GlitchlessGames extends JsonAPILoader.DefaultProfile {
        public GlitchlessGames(JsonAPILoader loader) { super(loader); }
        @Override public String getName()            { return "GlitchlessGames"; }
        @Override public int getPriority()           { return 700; }
        @Override public String getRoot()            { return "https://games.glitchless.ru/api/minecraft/users/profiles/textures/?nickname="; }
    }

    @Override
    public List<ICustomSkinLoaderPlugin.IDefaultProfile> getDefaultProfiles(JsonAPILoader loader) {
        return Lists.newArrayList(new GlitchlessGames(loader));
    }

    @Override
    public String toJsonUrl(String root, String username) {
        return root + username;
    }

    @Override
    public UserProfile toUserProfile(String root, String json, boolean local) {
        GlitchlessApiResponse result = new Gson().fromJson(json, GlitchlessApiResponse.class);
        if (!result.textures.containsKey(MinecraftProfileTexture.Type.SKIN))
            return null;

        return ModelManager0.toUserProfile(result.textures);
    }

    @Override
    public String getPayload(SkinSiteProfile ssp) {
        return null;
    }

    @Override
    public String getName() {
        return "GlitchlessAPI";
    }

    public static class GlitchlessApiResponse {
        protected Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures;
    }
}
