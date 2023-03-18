package customskinloader.fake;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import customskinloader.CustomSkinLoader;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.MinecraftUtil;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SkinManager.SkinAvailableCallback;
import net.minecraft.util.ResourceLocation;

public class FakeSkinManager {
    private final TextureManager textureManager;

    private final Map<ResourceLocation, MinecraftProfileTexture> modelCache = new ConcurrentHashMap<>();

    public FakeSkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) {
        this.textureManager = textureManagerInstance;
        HttpTextureUtil.defaultCacheDir = skinCacheDirectory;
    }

    /**
     * Invoked from {@link SkinManager#loadSkin(MinecraftProfileTexture, MinecraftProfileTexture.Type, SkinAvailableCallback)}
     */
    public static ResourceLocation setResourceLocation(ResourceLocation resourceLocation, MinecraftProfileTexture profileTexture) {
        if (profileTexture instanceof FakeMinecraftProfileTexture) {
            ((FakeMinecraftProfileTexture) profileTexture).setResourceLocation(resourceLocation);
        }
        return resourceLocation;
    }

    /**
     * Invoked from {@link SkinManager#loadSkin(MinecraftProfileTexture, MinecraftProfileTexture.Type, SkinAvailableCallback)}
     * <p>
     * {@code skinAvailableCallback.skinAvailable(textureType, resourcelocation, profileTexture);} -> {@code skinAvailableCallback.skinAvailable(textureType, resourcelocation, this.fakeManager.getModelCache(profileTexture, resourcelocation));}
     * <p>
     */
    public static MinecraftProfileTexture getModelCache(MinecraftProfileTexture profileTexture, FakeSkinManager fakeManager, ResourceLocation location) {
        return fakeManager.modelCache.getOrDefault(location, profileTexture);
    }

    /**
     * Invoked from {@link SkinManager#loadSkin(MinecraftProfileTexture, MinecraftProfileTexture.Type, SkinAvailableCallback)}
     */
    public static Object[] createThreadDownloadImageData(ImmutableList<Object> list, FakeSkinManager fakeManager, MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager.SkinAvailableCallback skinAvailableCallback) {
        Object[] params = list.toArray();
        if (profileTexture instanceof FakeMinecraftProfileTexture && params.length > 1) {
            FakeMinecraftProfileTexture fakeProfileTexture = (FakeMinecraftProfileTexture) profileTexture;
            ResourceLocation resourcelocation = fakeProfileTexture.getResourceLocation();
            if (fakeProfileTexture.getResourceLocation() != null) {
                params[0] = fakeProfileTexture.getCacheFile();
                if (params[params.length - 2] instanceof Boolean) {
                    params[params.length - 2] = true;
                }
                params[params.length - 1] = fakeManager.new BaseBuffer(skinAvailableCallback, textureType, resourcelocation, fakeProfileTexture);
            }
        }
        return params;
    }


    private final static String KEY = "CustomSkinLoaderInfo";
    /**
     * Invoked from {@link SkinManager#loadProfileTextures(GameProfile, SkinAvailableCallback, boolean)}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void loadProfileTextures(Runnable runnable, GameProfile profile) {
        CustomSkinLoader.loadProfileTextures(() -> CustomSkinLoader.loadProfileLazily(profile, m -> {
            // This is a hack.
            ((Multimap) profile.getProperties()).put(KEY, m);
            runnable.run();
        }));
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

    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = CustomSkinLoader.loadProfileFromCache(profile);
        for (Iterator<Map.Entry<MinecraftProfileTexture.Type, MinecraftProfileTexture>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<MinecraftProfileTexture.Type, MinecraftProfileTexture> entry = it.next();
            MinecraftProfileTexture texture = entry.getValue();
            if (shouldJudgeType(texture)) {
                texture = this.modelCache.get(MinecraftUtil.getSkinManager().loadSkin(texture, entry.getKey()));
                if (texture == null) { // remove texture if was not loaded before
                    it.remove();
                } else {
                    map.put(entry.getKey(), texture);
                }
            }
        }
        return map;
    }

    private static void makeCallback(SkinManager.SkinAvailableCallback callback, MinecraftProfileTexture.Type type, ResourceLocation location, MinecraftProfileTexture texture) {
        if (callback != null)
            callback.skinAvailable(type, location, texture);
    }

    private static boolean shouldJudgeType(MinecraftProfileTexture texture) {
        return texture != null && "auto".equals(texture.getMetadata("model"));
    }

    private class BaseBuffer implements IImageBuffer {
        private IImageBuffer buffer;

        private SkinManager.SkinAvailableCallback callback;
        private MinecraftProfileTexture.Type type;
        private ResourceLocation location;
        private FakeMinecraftProfileTexture texture;

        public BaseBuffer(SkinManager.SkinAvailableCallback callback, MinecraftProfileTexture.Type type, ResourceLocation location, FakeMinecraftProfileTexture texture) {
            switch (type) {
                case SKIN: this.buffer = new FakeSkinBuffer(); break;
                case CAPE: this.buffer = new FakeCapeBuffer(location); break;
            }

            this.callback = callback;
            this.type = type;
            this.location = location;
            this.texture = texture;
        }

        public net.minecraft.client.renderer.texture.NativeImage func_195786_a(net.minecraft.client.renderer.texture.NativeImage image) {
            return buffer instanceof FakeSkinBuffer ? ((FakeSkinBuffer) buffer).func_195786_a(image) : image;
        }

        public BufferedImage parseUserSkin(BufferedImage image) {
            return buffer instanceof FakeSkinBuffer ? ((FakeSkinBuffer) buffer).parseUserSkin(image) : image;
        }

        public void skinAvailable() {
            if (buffer != null) {
                buffer.skinAvailable();
                if (shouldJudgeType(texture) && buffer instanceof FakeSkinBuffer) {
                    //Auto judge skin type
                    Map<String, String> metadata = Maps.newHashMap();
                    String type = ((FakeSkinBuffer) buffer).judgeType();
                    metadata.put("model", type);
                    texture = new FakeMinecraftProfileTexture(texture.getRawUrl(), metadata);
                    FakeSkinManager.this.modelCache.put(location, texture);
                }
            }

            FakeSkinManager.makeCallback(callback, type, location, texture);
        }
    }
}
