package customskinloader.fake.texture;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import customskinloader.fake.FakeCapeBuffer;
import customskinloader.fake.FakeSkinBuffer;
import customskinloader.fake.FakeSkinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

// 24w46a+
public class FakeThreadDownloadImageData extends SimpleTexture {
    private FakeCapeBuffer buffer;
    private FakeNativeImage image;

    public static Function<NativeImage, CompletableFuture<?>> createTexture(Function<NativeImage, CompletableFuture<?>> function, ResourceLocation location, boolean bl) {
        return bl ? _image -> {
            if (location instanceof FakeResourceLocation && _image instanceof FakeNativeImage.Extended) {
                FakeSkinManager.BaseBuffer.judgeType(((FakeResourceLocation) location).getTexture(), () -> FakeSkinBuffer.judgeType0(((FakeNativeImage.Extended) _image).getFakeImage()));
            }
            return function.apply(_image);
        } : _image -> function.apply(_image).thenApply(_location -> {
            Minecraft.getMinecraft().getTextureManager().registerAndLoad(location, new FakeThreadDownloadImageData(location, _image));
            return _location;
        });
    }

    public FakeThreadDownloadImageData(ResourceLocation location, NativeImage image) {
        super(location);
        this.buffer = new FakeCapeBuffer();
        this.image = new FakeNativeImage(image);
    }

    @Override
    public TextureContents loadContents(IResourceManager resourceManager) {
        this.image = (FakeNativeImage) this.buffer.parseUserSkin(this.image);
        FakeNativeImage local = (FakeNativeImage) this.image.createImage(this.image.getWidth(), this.image.getHeight());
        local.copyImageData(this.image);
        return new TextureContents(local.getImage(), null);
    }
}
