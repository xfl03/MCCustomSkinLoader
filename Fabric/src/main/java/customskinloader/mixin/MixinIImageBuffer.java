package customskinloader.mixin;

import customskinloader.fake.itf.IFakeIImageBuffer;
import net.minecraft.client.renderer.IImageBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IImageBuffer.class)
public interface MixinIImageBuffer extends IFakeIImageBuffer {

}
