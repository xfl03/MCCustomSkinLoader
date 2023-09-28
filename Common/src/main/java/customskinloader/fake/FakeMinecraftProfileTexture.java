package customskinloader.fake;

import java.io.File;
import java.util.Map;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.utils.HttpTextureUtil;
import net.minecraft.util.ResourceLocation;

public class FakeMinecraftProfileTexture extends MinecraftProfileTexture {
    private final HttpTextureUtil.HttpTextureInfo info;
    private final Map<String, String> metadata;
    private ResourceLocation resourceLocation;

    public FakeMinecraftProfileTexture(String url, Map<String, String> metadata) {
        super(url, metadata);
        this.info = HttpTextureUtil.toHttpTextureInfo(url);
        this.metadata = metadata;
    }

    public void setResourceLocation(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }

    @Override
    public String getUrl() {
        return this.info.url;
    }

    public void setModule(String module) {
        if (this.metadata != null) {
            this.metadata.put("module", module);
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
