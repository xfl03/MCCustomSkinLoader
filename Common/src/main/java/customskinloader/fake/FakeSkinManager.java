package customskinloader.fake;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import customskinloader.CustomSkinLoader;
import customskinloader.utils.HttpTextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

public class FakeSkinManager {
    private final static Map<ResourceLocation, MinecraftProfileTexture> MODEL_CACHE = new ConcurrentHashMap<>();

    /**
     * Invoked from {@link SkinManager(net.minecraft.client.renderer.texture.TextureManager, File, MinecraftSessionService)}
     */
    public static void setSkinCacheDir(File skinCacheDirectory) {
        HttpTextureUtil.defaultCacheDir = skinCacheDirectory;
    }

    /**
     * Invoked from {@link SkinManager(net.minecraft.client.renderer.texture.TextureManager, Path, MinecraftSessionService, java.util.concurrent.Executor)}
     */
    public static void setSkinCacheDir(Path skinCacheDirectory) {
        HttpTextureUtil.defaultCacheDir = skinCacheDirectory.toFile();
    }

    private static CacheLoader<Object, ?> cacheLoader;
    /**
     * Invoked from {@link SkinManager(net.minecraft.client.renderer.texture.TextureManager, Path, MinecraftSessionService, java.util.concurrent.Executor)}
     */
    public static CacheLoader<Object, ?> setCacheLoader(CacheLoader<Object, ?> loader) {
        return cacheLoader = loader;
    }

    /**
     * Invoked from {@link SkinManager#getOrLoad(GameProfile)}
     */
    public static Object loadCache(LoadingCache<?, ?> loadingCache, Object cacheKey) throws Exception {
        return cacheLoader.load(cacheKey);
    }

    /**
     * Invoked from {@link SkinManager#loadSkin(MinecraftProfileTexture, MinecraftProfileTexture.Type, SkinManager.SkinAvailableCallback)}
     */
    public static ResourceLocation setResourceLocation(ResourceLocation resourceLocation, MinecraftProfileTexture profileTexture) {
        if (profileTexture instanceof FakeMinecraftProfileTexture) {
            ((FakeMinecraftProfileTexture) profileTexture).setResourceLocation(resourceLocation);
        }
        return resourceLocation;
    }

    /**
     * Invoked from {@link SkinManager#loadSkin(MinecraftProfileTexture, MinecraftProfileTexture.Type, SkinManager.SkinAvailableCallback)}
     * <p>
     * {@code skinAvailableCallback.skinAvailable(textureType, resourcelocation, profileTexture);} -> {@code skinAvailableCallback.skinAvailable(textureType, resourcelocation, this.fakeManager.getModelCache(profileTexture, resourcelocation));}
     * <p>
     */
    public static MinecraftProfileTexture getModelCache(MinecraftProfileTexture profileTexture, ResourceLocation location) {
        return MODEL_CACHE.getOrDefault(location, profileTexture);
    }

    /**
     * Invoked from {@link SkinManager#loadSkin(MinecraftProfileTexture, MinecraftProfileTexture.Type, SkinManager.SkinAvailableCallback)}
     */
    public static Object[] createThreadDownloadImageData(ImmutableList<Object> list, MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType) {
        Object[] params = list.toArray();
        if (profileTexture instanceof FakeMinecraftProfileTexture && params.length > 1) {
            FakeMinecraftProfileTexture fakeProfileTexture = (FakeMinecraftProfileTexture) profileTexture;
            ResourceLocation resourcelocation = fakeProfileTexture.getResourceLocation();
            if (fakeProfileTexture.getResourceLocation() != null) {
                params[0] = fakeProfileTexture.getCacheFile();
                if (params[params.length - 2] instanceof Boolean) {
                    params[params.length - 2] = true;
                }
                params[params.length - 1] = new BaseBuffer((Runnable) params[params.length - 1], textureType, resourcelocation, fakeProfileTexture);
            }
        }
        return params;
    }


    private final static String KEY = "CustomSkinLoaderInfo";
    /**
     * Invoked from {@link SkinManager#loadProfileTextures(GameProfile, SkinManager.SkinAvailableCallback, boolean)}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void loadProfileTextures(Runnable runnable, GameProfile profile) {
        CustomSkinLoader.loadProfileTextures(() -> CustomSkinLoader.loadProfileLazily(profile, m -> {
            // This is a hack.
            ((Multimap) profile.getProperties()).put(KEY, m);
            runnable.run();
            return null;
        }));
    }

    /**
     * 23w31a+
     * Invoked from net.minecraft.client.resources.SkinManager$1#load(net.minecraft.client.resources.SkinManager$CacheKey, GameProfile)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object[] loadProfileTextures(ImmutableList<Object> list, GameProfile profile) {
        Object[] params = list.toArray();
        if (!profile.getProperties().containsKey(SKULL_KEY)) {
            final Supplier<?> supplier = (Supplier<?>) params[0];
            params[0] = (Supplier<?>) () -> CustomSkinLoader.loadProfileLazily(profile, m -> {
                // This is a hack.
                ((Multimap) profile.getProperties()).put(KEY, m);
                return supplier.get();
            });
            params[1] = CustomSkinLoader.THREAD_POOL;
        } else {
            params[1] = Minecraft.getMinecraft();
        }
        return params;
    }

    /**
     * Invoked from {@link SkinManager#lambda$loadProfileTextures$1(GameProfile, boolean, SkinAvailableCallback)}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getUserProfile(MinecraftSessionService sessionService, GameProfile profile, boolean requireSecure) {
        // This is a hack.
        return (Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>) ((Optional) profile.getProperties().removeAll(KEY).stream().findFirst()).orElse(Maps.newHashMap());
    }

    /**
     * Invoked from {@link SkinManager#lambda$null$0(Map, SkinAvailableCallback)}
     */
    public static void loadElytraTexture(SkinManager skinManager, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map, SkinManager.SkinAvailableCallback skinAvailableCallback) {
        for (int i = 2; i < MinecraftProfileTexture.Type.values().length; i++) {
            MinecraftProfileTexture.Type type = MinecraftProfileTexture.Type.values()[i];
            if (map.containsKey(type)) {
                skinManager.loadSkin(map.get(type), type, skinAvailableCallback);
            }
        }
    }

    private final static String SKULL_KEY = "CSL$IsSkull";
    /**
     * 23w31a+
     * Invoked from {@link SkinManager#getInsecureSkin(GameProfile)}
     */
    public static void setSkullType(GameProfile profile) {
        profile.getProperties().removeAll(SKULL_KEY);
        profile.getProperties().put(SKULL_KEY, new Property(SKULL_KEY, "true"));
    }

    /**
     * 23w31a+
     * Invoked from net.minecraft.client.resources.SkinManager$1#lambda$load$0(GameProfile)
     */
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(MinecraftSessionService sessionService, GameProfile profile, boolean requireSecure) {
        if (profile.getProperties().containsKey(SKULL_KEY)) {
            profile.getProperties().removeAll(SKULL_KEY);
            return CustomSkinLoader.loadProfileFromCache(profile);
        } else {
            return getUserProfile(sessionService, profile, requireSecure);
        }
    }

    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        return CustomSkinLoader.loadProfileFromCache(profile);
    }

    private static boolean shouldJudgeType(MinecraftProfileTexture texture) {
        return texture != null && "auto".equals(texture.getMetadata("model"));
    }

    private static class BaseBuffer implements IImageBuffer {
        private IImageBuffer buffer;

        private final Runnable callback;
        private final ResourceLocation location;
        private final FakeMinecraftProfileTexture texture;

        public BaseBuffer(Runnable callback, MinecraftProfileTexture.Type type, ResourceLocation location, FakeMinecraftProfileTexture texture) {
            switch (type) {
                case SKIN: this.buffer = new FakeSkinBuffer(); break;
                case CAPE: this.buffer = new FakeCapeBuffer(location); break;
            }

            this.callback = callback;
            this.location = location;
            this.texture = texture;
        }

        @Override
        public NativeImage func_195786_a(NativeImage image) {
            return buffer instanceof FakeSkinBuffer ? ((FakeSkinBuffer) buffer).func_195786_a(image) : image;
        }

        @Override
        public BufferedImage parseUserSkin(BufferedImage image) {
            return buffer instanceof FakeSkinBuffer ? ((FakeSkinBuffer) buffer).parseUserSkin(image) : image;
        }

        @Override
        public void skinAvailable() {
            if (buffer != null) {
                buffer.skinAvailable();
                if (shouldJudgeType(texture) && buffer instanceof FakeSkinBuffer) {
                    //Auto judge skin type
                    String type = ((FakeSkinBuffer) buffer).judgeType();
                    texture.setModule(type);
                    MODEL_CACHE.put(location, texture);
                }
            }

            if (this.callback != null) {
                this.callback.run();
            }
        }
    }
}
