package customskinloader.fake;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.utils.HttpTextureUtil;

public class FakeMinecraftProfileTexture extends MinecraftProfileTexture {
    private final static Map<String, String> MODEL_CACHE = new ConcurrentHashMap<>();

    private final HttpTextureUtil.HttpTextureInfo info;
    private final Map<String, String> metadata;

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
    public String getMetadata(final String key) {
        String value = super.getMetadata(key);
        if ("model".equals(key) && "auto".equals(value)) {
            String model = MODEL_CACHE.get(this.getHash());
            if (model != null) {
                return model;
            }
        }
        return value;
    }

    public void setModel(String model) {
        if (this.metadata != null) {
            MODEL_CACHE.put(this.getHash(), model);
            this.metadata.put("model", model);
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
