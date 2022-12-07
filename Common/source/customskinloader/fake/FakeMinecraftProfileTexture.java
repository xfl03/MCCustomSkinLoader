package customskinloader.fake;

import java.io.File;
import java.util.Map;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.utils.HttpTextureUtil;
import net.minecraft.util.ResourceLocation;

public class FakeMinecraftProfileTexture extends MinecraftProfileTexture {
    private HttpTextureUtil.HttpTextureInfo info;
    private ResourceLocation resourceLocation;

    public FakeMinecraftProfileTexture(String url, Map<String, String> metadata) {
        super(url, metadata);
        this.info = HttpTextureUtil.toHttpTextureInfo(url);
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

    @Override
    public String getHash() {
        return this.info.hash;
    }

    public File getCacheFile() {
        return this.info.cacheFile;
    }
}
