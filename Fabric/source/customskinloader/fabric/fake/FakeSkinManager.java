package customskinloader.fabric.fake;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import customskinloader.CustomSkinLoader;
import customskinloader.utils.HttpTextureUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.ImageFilter;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.Texture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

public class FakeSkinManager {
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
    private final TextureManager textureManager;

    public FakeSkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) {
        this.textureManager = textureManagerInstance;
        HttpTextureUtil.defaultCacheDir = skinCacheDirectory;
    }

    public Identifier loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType) {
        return this.loadSkin(profileTexture, textureType, null);
    }

    public Identifier loadSkin(final MinecraftProfileTexture profileTexture, final MinecraftProfileTexture.Type textureType, final PlayerSkinProvider.SkinTextureAvailableCallback skinAvailableCallback) {
        HttpTextureUtil.HttpTextureInfo info = HttpTextureUtil.toHttpTextureInfo(profileTexture.getUrl());

        final Identifier resourcelocation = new Identifier("skins/" + info.hash);
        Texture itextureobject = this.textureManager.getTexture(resourcelocation);

        if (itextureobject != null) {//Have already loaded
            makeCallback(skinAvailableCallback, textureType, resourcelocation, profileTexture);
        } else {
            PlayerSkinTexture threaddownloadimagedata = new PlayerSkinTexture(info.cacheFile, info.url, DefaultSkinHelper.getTexture(), new BaseBuffer(skinAvailableCallback, textureType, resourcelocation, profileTexture));
            if (skinAvailableCallback instanceof FakeClientPlayer.LegacyBuffer) {//Cache for client player
                FakeClientPlayer.textureCache.put(resourcelocation, threaddownloadimagedata);
            }
            this.textureManager.registerTexture(resourcelocation, threaddownloadimagedata);
        }
        return resourcelocation;
    }

    public void loadProfileTextures(final GameProfile profile, final PlayerSkinProvider.SkinTextureAvailableCallback skinAvailableCallback, final boolean requireSecure) {
        FakeSkinManager.THREAD_POOL.submit(new Runnable() {
            @Override
            public void run() {
                final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Maps.newHashMap();
                map.putAll(CustomSkinLoader.loadProfile(profile));

                MinecraftClient.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        for (MinecraftProfileTexture.Type type : MinecraftProfileTexture.Type.values()) {
                            if (map.containsKey(type)) {
                                CustomSkinLoader.logger.debug("Loading type:" + type);
                                FakeSkinManager.this.loadSkin(map.get(type), type, skinAvailableCallback);
                            }
                        }
                    }
                });
            }
        });
    }

    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        return CustomSkinLoader.loadProfileFromCache(profile);
    }

    private static void makeCallback(PlayerSkinProvider.SkinTextureAvailableCallback callback, MinecraftProfileTexture.Type type, Identifier location, MinecraftProfileTexture texture) {
        if (callback != null) {
            callback.onSkinTextureAvailable(type, location, texture);
        }
    }

    private class BaseBuffer implements ImageFilter {
        private ImageFilter buffer;

        private PlayerSkinProvider.SkinTextureAvailableCallback callback;
        private MinecraftProfileTexture.Type type;
        private Identifier location;
        private MinecraftProfileTexture texture;

        public BaseBuffer(PlayerSkinProvider.SkinTextureAvailableCallback callback, MinecraftProfileTexture.Type type, Identifier location, MinecraftProfileTexture texture) {
            this.buffer = (type == MinecraftProfileTexture.Type.SKIN ? new FakeSkinBuffer() : null);

            this.callback = callback;
            this.type = type;
            this.location = location;
            this.texture = texture;
        }

        @Override
        public NativeImage filterImage(NativeImage image) {
            return this.buffer instanceof FakeSkinBuffer ? ((FakeSkinBuffer) this.buffer).filterImage(image) : image;
        }

        @Override
        public void method_3238() {
            if (this.buffer != null) {
                this.buffer.method_3238();
                if ("auto".equals(this.texture.getMetadata("model")) && this.buffer instanceof FakeSkinBuffer) {
                    //Auto judge skin type
                    Map<String, String> metadata = Maps.newHashMap();
                    metadata.put("model", ((FakeSkinBuffer) this.buffer).judgeType());
                    this.texture = new MinecraftProfileTexture(this.texture.getUrl(), metadata);
                }
            }

            FakeSkinManager.makeCallback(this.callback, this.type, this.location, this.texture);
        }
    }
}
