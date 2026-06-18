package customskinloader.loader.jsonapi;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.MojangAPILoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.UserProfile;

public class MinecraftCapesAPI implements JsonAPILoader.IJsonAPI {

    public static class MinecraftCapes extends JsonAPILoader.DefaultProfile {
        public MinecraftCapes(JsonAPILoader loader) {
            super(loader);
        }

        @Override
        public String getName() {
            return "MinecraftCapes";
        }

        @Override
        public int getPriority() {
            return 800;
        }

        @Override
        public String getRoot() {
            return "https://api.minecraftcapes.net/profile/";
        }
    }

    @Override
    public List<ICustomSkinLoaderPlugin.IDefaultProfile> getDefaultProfiles(JsonAPILoader loader) {
        return Lists.newArrayList(new MinecraftCapes(loader));
    }

    @Override
    public String toJsonUrl(String root, String username) {
        String uuid = MojangAPILoader.getMojangUuidByUsername(username, false);
        //If uuid cannot be found, we won't load profile in this API.
        if (uuid == null) {
            return null;
        }
        //API url is `${root}${uuid}`
        return root + uuid;
    }

    @Override
    public UserProfile toUserProfile(String root, String json, boolean local) {
        MinecraftCapesApiResponse result = new Gson().fromJson(json, MinecraftCapesApiResponse.class);
        if (result.capeUrl == null) {
            return null;
        }

        UserProfile profile = new UserProfile();
        profile.capeUrl = result.capeUrl;

        return profile;
    }

    @Override
    public String getName() {
        return "MinecraftCapesAPI";
    }

    public static class MinecraftCapesApiResponse {
        @SerializedName("cape_url")
        public String capeUrl;
    }
}
