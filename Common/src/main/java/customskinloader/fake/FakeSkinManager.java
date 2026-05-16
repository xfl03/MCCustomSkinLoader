package customskinloader.fake;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.platform.NativeImage;
import customskinloader.CustomSkinLoader;
import customskinloader.fake.itf.FakeHttpTextureProcessor;
import customskinloader.profile.ModelManager0;
import customskinloader.utils.HttpTextureUtil;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SkinManager$CacheKey;
import net.minecraft.server.Services;

public class FakeSkinManager {
    /**
     * 1.20.1-
     * Invoked from {@link SkinManager(TextureManager, File, MinecraftSessionService)}
     */
    public static void setSkinCacheDir(File skinCacheDirectory) {
        HttpTextureUtil.defaultCacheDir = skinCacheDirectory;
    }

    /**
     * 23w31a ~ 23w45a
     * Invoked from {@link SkinManager(TextureManager, Path, MinecraftSessionService, Executor)}
     *
     * 23w46a ~ 25w33a
     * Invoked from {@link SkinManager(Path, MinecraftSessionService, Executor)}
     *
     * 25w34a+
     * Invoked from {@link SkinManager(Path, Services, Executor)}
     */
    public static void setSkinCacheDir(Path skinCacheDirectory) {
        HttpTextureUtil.defaultCacheDir = skinCacheDirectory.toFile();
    }

    /**
     * 1.20.1-
     * Invoked from {@link SkinManager#registerSkins(GameProfile, SkinManager$SkinTextureCallback, boolean)}
     */
    public static void loadProfileTextures(Runnable runnable) {
        CustomSkinLoader.loadProfileTextures(runnable);
    }

    /**
     * 23w31a+
     * Invoked from {@link SkinManager$1#load(SkinManager$CacheKey)}
     */
    public static Executor loadProfileTextures(Executor executor) {
        return CustomSkinLoader.THREAD_POOL;
    }

    /**
     * 1.20.1-
     * Invoked from {@link SkinManager#lambda$registerSkins$4(GameProfile, boolean, SkinManager$SkinTextureCallback)}
     */
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getUserProfile(MinecraftSessionService sessionService, GameProfile profile, boolean requireSecure) {
        return ModelManager0.fromUserProfile(CustomSkinLoader.loadProfile(profile));
    }

    /**
     * 1.20.1-
     * Invoked from {@link SkinManager#getInsecureSkinInformation(GameProfile)}
     */
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        return CustomSkinLoader.loadProfileFromCache(profile);
    }

    /**
     * 23w31a ~ 23w41a
     * Invoked from {@link SkinManager$1#lambda$load$0(MinecraftSessionService, GameProfile)}
     */
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(MinecraftSessionService sessionService, GameProfile profile, boolean requireSecure) {
        return getUserProfile(sessionService, profile, requireSecure);
    }

    /**
     * 23w42a ~ 25w33a
     * Invoked from {@link SkinManager$1#lambda$load$0(SkinManager$CacheKey, MinecraftSessionService)}
     *
     * 25w34a+
     * Invoked from {@link SkinManager$1#lambda$load$0(SkinManager$CacheKey, Services)}
     */
    public static Object loadSkinFromCache(MinecraftSessionService sessionService, Property property, SkinManager$CacheKey cacheKey) {
        if (cacheKey instanceof FakeCacheKey) {
            return FakeCacheKey.createMinecraftProfileTextures(loadSkinFromCache(sessionService, ((FakeCacheKey) cacheKey).profile(), false));
        }
        return sessionService.unpackTextures(property);
    }

    public static class BaseBuffer implements FakeHttpTextureProcessor {
        private FakeHttpTextureProcessor buffer;

        private final Runnable callback;
        private final FakeMinecraftProfileTexture texture;

        public BaseBuffer(Runnable callback, MinecraftProfileTexture.Type type, FakeMinecraftProfileTexture texture) {
            this.callback = callback;
            this.texture = texture;

            switch (type) {
                case SKIN: this.buffer = new FakeSkinBuffer(); break;
                case CAPE: this.buffer = new FakeCapeBuffer(); break;
            }
        }

        @Override
        public NativeImage process(NativeImage image) {
            return this.buffer instanceof FakeSkinBuffer ? this.buffer.process(image) : image;
        }

        @Override
        public BufferedImage process(BufferedImage image) {
            return this.buffer instanceof FakeSkinBuffer ? this.buffer.process(image) : image;
        }

        @Override
        public void onTextureDownloaded() {
            if (this.buffer != null) {
                this.buffer.onTextureDownloaded();
                if (this.buffer instanceof FakeSkinBuffer) {
                    judgeType(this.texture, () -> ((FakeSkinBuffer) this.buffer).judgeType());
                }
            }

            if (this.callback != null) {
                this.callback.run();
            }
        }

        public static void judgeType(FakeMinecraftProfileTexture texture, Supplier<String> type) {
            if (shouldJudgeType(texture)) {
                //Auto judge skin type
                texture.setModel(type.get());
            }
        }

        private static boolean shouldJudgeType(FakeMinecraftProfileTexture texture) {
            return texture != null && "auto".equals(texture.getMetadata("model", false));
        }
    }

    public static class FakeCacheKey extends SkinManager$CacheKey {
        private final GameProfile profile;

        public FakeCacheKey(UUID uuid, Property property, GameProfile profile) {
            super(uuid, property);
            this.profile = profile;
        }

        public GameProfile profile() {
            return this.profile;
        }

        /**
         * 23w42a ~ 25w33a
         * Invoked from {@link SkinManager#getOrLoad(GameProfile)}
         *
         * 25w34a+
         * Invoked from {@link SkinManager#get(GameProfile)}
         */
        public static SkinManager$CacheKey createFakeCacheKey(UUID uuid, Property property, GameProfile profile) {
            return new FakeCacheKey(uuid, property == null ? new Property(null, null) : property, profile);
        }

        public static Object createMinecraftProfileTextures(Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures) {
            return new MinecraftProfileTextures(textures.get(MinecraftProfileTexture.Type.SKIN), textures.get(MinecraftProfileTexture.Type.CAPE), textures.get(MinecraftProfileTexture.Type.ELYTRA), SignatureState.SIGNED);
        }
    }
}
