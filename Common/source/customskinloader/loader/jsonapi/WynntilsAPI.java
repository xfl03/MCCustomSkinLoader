package customskinloader.loader.jsonapi;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.MojangAPILoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.UserProfile;
import customskinloader.utils.TextureUtil;

import java.util.List;

/**
 * Wynntils cape API
 * <a href="https://wynntils.com/">Home</a>
 * Test player: Cael
 *
 * @since 14.16
 */
public class WynntilsAPI implements JsonAPILoader.IJsonAPI {

    public static class Wynntils extends JsonAPILoader.DefaultProfile {
        public Wynntils(JsonAPILoader loader) {
            super(loader);
        }

        @Override
        public String getName() {
            return "Wynntils";
        }

        @Override
        public int getPriority() {
            return 820;
        }

        @Override
        public String getRoot() {
            return "https://athena.wynntils.com/user/getInfo";
        }
    }

    @Override
    public List<ICustomSkinLoaderPlugin.IDefaultProfile> getDefaultProfiles(JsonAPILoader loader) {
        return Lists.newArrayList(new Wynntils(loader));
    }

    @Override
    public String toJsonUrl(String root, String username) {
        return root;
    }

    @Override
    public UserProfile toUserProfile(String root, String json, boolean local) {
        WynntilsApiResponse result = new Gson().fromJson(json, WynntilsApiResponse.class);
        if (result.user == null || result.user.cosmetics == null || result.user.cosmetics.texture == null) {
            return null;
        }

        UserProfile profile = new UserProfile();
        String fakeUrl = TextureUtil.parseBase64Texture(result.user.cosmetics.texture);
        if (result.user.cosmetics.hasCape) {
            profile.capeUrl = fakeUrl;
        } else if (result.user.cosmetics.hasElytra) {
            profile.elytraUrl = fakeUrl;
        } else {
            CustomSkinLoader.logger.warning("Illegal response found in Wynntils");
            return null;
        }

        return profile;
    }

    @Override
    public String getPayload(SkinSiteProfile ssp, String username) {
        WynntilsApiRequest req = new WynntilsApiRequest();
        req.uuid = MojangAPILoader.getMojangUuidByUsername(username, true);
        if (req.uuid == null) {
            throw new JsonAPILoader.ProfileNotFoundException();
        }
        return new Gson().toJson(req);
    }

    @Override
    public String getName() {
        return "WynntilsAPI";
    }

    public static class WynntilsApiRequest {
        public String uuid;
    }

    public static class WynntilsApiResponse {
        public User user;

        public static class User {
            public String accountType;
            public Cosmetics cosmetics;

            public static class Cosmetics {
                public boolean hasCape;
                public boolean hasElytra;
                public boolean hasEars;
                public String texture;
            }
        }
    }
}
