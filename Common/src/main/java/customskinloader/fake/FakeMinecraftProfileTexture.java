package customskinloader.fake;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.CustomSkinLoader;
import customskinloader.utils.HttpTextureUtil;

public class FakeMinecraftProfileTexture extends MinecraftProfileTexture {
    private final static Map<String, String> MODEL_CACHE = new ConcurrentHashMap<>();

    private final HttpTextureUtil.HttpTextureInfo info;
    private final Map<String, String> metadata;
    private final CountDownLatch latch = new CountDownLatch(1);

    public FakeMinecraftProfileTexture(String url, Map<String, String> metadata) {
        super(url, metadata);
        this.info = HttpTextureUtil.toHttpTextureInfo(url);
        this.metadata = metadata;
    }

    @Override
    public String getUrl() {
        return this.info.url;
    }

    @Override
    public String getMetadata(String key) {
        return this.getMetadata(key, true);
    }

    public String getMetadata(String key, boolean lock) {
        String value = super.getMetadata(key);
        if ("model".equals(key) && "auto".equals(value)) {
            String model = MODEL_CACHE.get(this.getHash());
            if (model != null) {
                return model;
            } else {
                try {
                    if (lock && this.latch.await(60L * CustomSkinLoader.config.loadlist.size(), TimeUnit.SECONDS)) {
                        return this.getMetadata(key, false);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return value;
    }

    public void setModel(String model) {
        if (this.metadata != null) {
            MODEL_CACHE.put(this.getHash(), model);
            this.metadata.put("model", model);
            this.latch.countDown();
        }
    }

    @Override
    public String getHash() {
        return this.info.hash == null ? super.getHash() : this.info.hash;
    }

    public File getCacheFile() {
        return this.info.cacheFile;
    }
}
