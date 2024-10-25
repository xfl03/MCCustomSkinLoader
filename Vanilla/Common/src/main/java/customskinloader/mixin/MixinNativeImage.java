package customskinloader.mixin;

import customskinloader.fake.itf.IFakeNativeImage;
import net.minecraft.client.renderer.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NativeImage.class)
public abstract class MixinNativeImage implements IFakeNativeImage {

}
