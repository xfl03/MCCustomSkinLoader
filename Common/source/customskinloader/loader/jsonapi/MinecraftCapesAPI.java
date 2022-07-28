package customskinloader.loader.jsonapi;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.CustomSkinLoader;
import customskinloader.Logger;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.MojangAPILoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpTextureUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

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
            return "https://minecraftcapes.net/profile/";
        }
    }

    @Override
    public List<ICustomSkinLoaderPlugin.IDefaultProfile> getDefaultProfiles(JsonAPILoader loader) {
        return Lists.newArrayList(new MinecraftCapes(loader));
    }

    @Override
    public String toJsonUrl(String root, String username) {
        String uuid = MojangAPILoader.getMojangUuidByUsername(username);
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
        if (result.textures == null || result.textures.cape == null) {
            return null;
        }

        String capeBase64 = result.textures.cape;
        byte[] capeBytes = Base64.decodeBase64(capeBase64);
        String hash = HttpTextureUtil.getHash(capeBytes);
        File cacheFile = HttpTextureUtil.getCacheFile(hash);
        String fakeUrl = HttpTextureUtil.getBase64FakeUrl(hash);

        //Save base64 image to cache file
        try {
            FileUtils.writeByteArrayToFile(cacheFile, capeBytes);
            CustomSkinLoader.logger.info("Saved base64 image to " + cacheFile);
        } catch (Exception e) {
            CustomSkinLoader.logger.warning("Error parsing base64 image: " + capeBase64);
            return null;
        }

        UserProfile profile = new UserProfile();
        profile.capeUrl = fakeUrl;

        return profile;
    }

    @Override
    public String getPayload(SkinSiteProfile ssp) {
        return null;
    }

    @Override
    public String getName() {
        return "MinecraftCapesAPI";
    }

    public static class MinecraftCapesApiResponse {
        public boolean animatedCape;
        public boolean capeGlint;
        public boolean upsideDown;
        public MinecraftCapesApiTexture textures;

        public static class MinecraftCapesApiTexture {
            public String cape;
            public String ears;
        }
    }
}
