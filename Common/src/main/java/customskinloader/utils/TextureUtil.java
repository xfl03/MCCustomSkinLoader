package customskinloader.utils;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
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

    private final static MethodHandles.Lookup IMPL_LOOKUP = ((Supplier<MethodHandles.Lookup>) () -> {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Object theUnsafe = theUnsafeField.get(null);

            Method getObjectMethod = unsafeClass.getMethod("getObject", Object.class, long.class);
            Method staticFieldBaseMethod = unsafeClass.getMethod("staticFieldBase", Field.class);
            Method staticFieldOffsetMethod = unsafeClass.getMethod("staticFieldOffset", Field.class);

            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            return (MethodHandles.Lookup) getObjectMethod.invoke(theUnsafe, staticFieldBaseMethod.invoke(theUnsafe, implLookupField), staticFieldOffsetMethod.invoke(theUnsafe, implLookupField));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }).get();

    // Some of the classes in Authlib used after Minecraft 23w31a were changed to record classes,
    // resulting in changes to method names, so reflection is used here to be compatible with these changes.
    public enum AuthlibField {
        PROPERTY_NAME(Property.class, "name", String.class),
        PROPERTY_VALUE(Property.class, "value", String.class),
        PROPERTY_SIGNATURE(Property.class, "signature", String.class),
        MINECRAFT_PROFILE_PROPERTIES_RESPONSE_ID(MinecraftProfilePropertiesResponse.class, "id", UUID.class),
        MINECRAFT_PROFILE_PROPERTIES_RESPONSE_NAME(MinecraftProfilePropertiesResponse.class, "name", String.class),
        MINECRAFT_PROFILE_PROPERTIES_RESPONSE_PROPERTIES(MinecraftProfilePropertiesResponse.class, "properties", PropertyMap.class),
        MINECRAFT_TEXTURES_PAYLOAD_TEXTURES(MinecraftTexturesPayload.class, "textures", Map.class);

        private final MethodHandle getter;
        private final MethodHandle setter;

        AuthlibField(Class<?> clazz, String name, Class<?> returnType) {
            try {
                this.getter = IMPL_LOOKUP.findGetter(clazz, name, returnType);
                this.setter = IMPL_LOOKUP.findSetter(clazz, name, returnType);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @SuppressWarnings("unchecked")
        public <R> R get(Object o) {
            try {
                return (R) this.getter.invokeWithArguments(o);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        public void set(Object o, Object value) {
            try {
                this.setter.invokeWithArguments(o, value);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    public static String fromUUID(UUID value) {
        return value.toString().replace("-", "");
    }
}
