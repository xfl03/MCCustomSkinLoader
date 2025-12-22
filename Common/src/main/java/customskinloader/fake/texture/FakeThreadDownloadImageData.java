package customskinloader.fake.texture;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import customskinloader.fake.FakeCapeBuffer;
import customskinloader.fake.FakeMinecraftProfileTexture;
import customskinloader.fake.FakeSkinBuffer;
import customskinloader.fake.FakeSkinManager;
import customskinloader.fake.itf.FakeInterfaceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ResourceLocation;

// 24w46a+
public class FakeThreadDownloadImageData extends SimpleTexture {
    private FakeCapeBuffer buffer;
    private FakeNativeImage image;

    public static Function<NativeImage, CompletableFuture<?>> createTextureV1(Function<NativeImage, CompletableFuture<?>> function, ResourceLocation location, boolean bl) {
        return createTexture(function, _image -> _location -> {
            Minecraft.getMinecraft().getTextureManager().registerAndLoad(location, new FakeThreadDownloadImageData(location, copyImage(new FakeNativeImage(_image))));
            return _location;
        }, location, bl);
    }

    public static Function<NativeImage, CompletableFuture<?>> createTextureV2(Function<NativeImage, CompletableFuture<?>> function, Identifier identifier, boolean bl) {
        return createTexture(function, _image -> _location -> {
            Minecraft.getMinecraft().getTextureManager().registerAndLoad(identifier, new FakeThreadDownloadImageData(identifier, copyImage(new FakeNativeImage(_image)), null));
            return _location;
        }, identifier, bl);
    }

    public static Function<NativeImage, CompletableFuture<?>> createTexture(Function<NativeImage, CompletableFuture<?>> function, Function<NativeImage, Function<Object, Object>> cape, Object location, boolean bl) {
        return bl ? _image -> {
            FakeMinecraftProfileTexture texture = FakeInterfaceManager.ResourceLocation_getTexture(location);
            FakeNativeImage image = FakeInterfaceManager.NativeImage_getFakeImage(_image);
            if (texture != null && image != null) {
                FakeSkinManager.BaseBuffer.judgeType(texture, () -> FakeSkinBuffer.judgeType0(image));
            }
            return function.apply(_image);
        } : _image -> function.apply(_image).thenApply(cape.apply(_image));
    }

    public FakeThreadDownloadImageData(ResourceLocation location, FakeNativeImage image) {
        super(location);
        this.buffer = new FakeCapeBuffer();
        this.image = image;
    }

    public FakeThreadDownloadImageData(Identifier identifier, FakeNativeImage image, Object o) {
        super(identifier);
        this.buffer = new FakeCapeBuffer();
        this.image = image;
    }

    @Override
    public TextureContents loadContents(IResourceManager resourceManager) {
        this.image = (FakeNativeImage) this.buffer.parseUserSkin(this.image);
        return new TextureContents(copyImage(this.image).getImage(), null);
    }

    /**
     * Protects cached NativeImage instances from external interference (e.g. accidental close operations). All lifecycle management is handled internally by this cache.
     */
    private static FakeNativeImage copyImage(FakeNativeImage from) {
        FakeNativeImage local = (FakeNativeImage) from.createImage(from.getWidth(), from.getHeight());
        local.copyImageData(from);
        return local;
    }
}
