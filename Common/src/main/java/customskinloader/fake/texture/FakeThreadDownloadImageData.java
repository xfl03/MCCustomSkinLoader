package customskinloader.fake.texture;

import java.nio.file.Path;

import customskinloader.fake.FakeSkinBuffer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

// 24w46a+
public class FakeThreadDownloadImageData extends SimpleTexture {
    private final Path cacheFile;
    private final String imageUrl;
    private final boolean legacySkin;
    private final Runnable processTask;

    public static Object createTexture(Object location, Object cacheFile, Object imageUrl, Object legacySkin, Object processTask) {
        return new FakeThreadDownloadImageData((ResourceLocation) location, (Path) cacheFile, (String) imageUrl, (boolean) legacySkin, (Runnable) processTask);
    }

    public FakeThreadDownloadImageData(ResourceLocation location, Path cacheFile, String imageUrl, boolean legacySkin, Runnable processTask) {
        super(location);
        this.cacheFile = cacheFile;
        this.imageUrl = imageUrl;
        this.legacySkin = legacySkin;
        this.processTask = processTask;
    }

    @Override
    public TextureContents loadContents(IResourceManager resourceManager) {
        NativeImage image = FakeSkinBuffer.processLegacySkin(SkinTextureDownloader.lambda$downloadAndRegisterSkin$0(this.cacheFile, this.imageUrl, this.legacySkin), this.processTask, i -> SkinTextureDownloader.processLegacySkin(i, this.imageUrl));
        this.processTask.run();
        return new TextureContents(image, null);
    }
}
