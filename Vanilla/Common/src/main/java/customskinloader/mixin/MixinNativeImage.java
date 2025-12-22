package customskinloader.mixin;

import customskinloader.fake.itf.IFakeNativeImage;
import customskinloader.fake.texture.FakeNativeImage;
import net.minecraft.client.renderer.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NativeImage.class)
public abstract class MixinNativeImage implements IFakeNativeImage {
    private FakeNativeImage fakeImage;

    @Override
    public FakeNativeImage getFakeImage() {
        return this.fakeImage;
    }

    @Override
    public void setFakeImage(FakeNativeImage fakeImage) {
        this.fakeImage = fakeImage;
    }
}
