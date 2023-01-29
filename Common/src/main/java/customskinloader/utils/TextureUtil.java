package customskinloader.utils;

import customskinloader.CustomSkinLoader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class TextureUtil {
    /**
     * Save and get fake url of base64 texture
     *
     * @param base64 texture in base64 string
     * @return base64 fake url
     * @since 14.16
     */
    public static String parseBase64Texture(String base64) {
        byte[] capeBytes = Base64.decodeBase64(base64);
        String hash = HttpTextureUtil.getHash(capeBytes);
        File cacheFile = HttpTextureUtil.getCacheFile(hash);
        String fakeUrl = HttpTextureUtil.getBase64FakeUrl(hash);

        //Save base64 image to cache file
        try {
            FileUtils.writeByteArrayToFile(cacheFile, capeBytes);
            CustomSkinLoader.logger.info("Saved base64 image to " + cacheFile);
            return fakeUrl;
        } catch (Exception e) {
            CustomSkinLoader.logger.warning("Error parsing base64 image: " + base64);
            return null;
        }
    }
}
