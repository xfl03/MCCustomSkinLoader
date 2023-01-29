package customskinloader.mixin;

import customskinloader.fake.itf.IFakeIResource;
import net.minecraft.client.resources.IResource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IResource.class)
public interface MixinIResource extends IFakeIResource.V1, IFakeIResource.V2 {

}
