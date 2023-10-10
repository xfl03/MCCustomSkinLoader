package customskinloader.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
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
    public enum AuthlibField {
        PROPERTY_VALUE_FIELD(Property.class, "value"),
        MINECRAFT_PROFILE_PROPERTIES_RESPONSE_ID(MinecraftProfilePropertiesResponse.class, "id"),
        MINECRAFT_PROFILE_PROPERTIES_RESPONSE_NAME(MinecraftProfilePropertiesResponse.class, "name"),
        MINECRAFT_PROFILE_PROPERTIES_RESPONSE_PROPERTIES(MinecraftProfilePropertiesResponse.class, "properties"),
        MINECRAFT_TEXTURES_PAYLOAD_TEXTURES(MinecraftTexturesPayload.class, "textures");

        private final Field field;

        AuthlibField(Class<?> clazz, String name) {
            try {
                this.field = clazz.getDeclaredField(name);
                this.field.setAccessible(true);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T get(Object o) {
            try {
                return (T) this.field.get(o);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    public static String fromUUID(UUID value) {
        return value.toString().replace("-", "");
    }
}
