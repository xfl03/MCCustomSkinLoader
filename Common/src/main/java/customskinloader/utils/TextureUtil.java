package customskinloader.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import customskinloader.CustomSkinLoader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

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


    // Some of the classes in Authlib used after Minecraft 23w31a were changed to record classes,
    // resulting in changes to method names, so reflection is used here to be compatible with these changes.
    private final static Field VALUE_FIELD;
    private final static Field TEXTURES_FIELD;

    static {
        try {
            VALUE_FIELD = Property.class.getDeclaredField("value");
            VALUE_FIELD.setAccessible(true);
            TEXTURES_FIELD = MinecraftTexturesPayload.class.getDeclaredField("textures");
            TEXTURES_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPropertyValue(Property property) {
        try {
            return (String) VALUE_FIELD.get(property);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getMinecraftTexturesPayloadTextures(MinecraftTexturesPayload payload) {
        try {
            return (Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>) TEXTURES_FIELD.get(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String fromUUID(UUID value) {
        return value.toString().replace("-", "");
    }
}
