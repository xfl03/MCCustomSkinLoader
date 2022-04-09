package customskinloader.mixin;

import customskinloader.fake.itf.IFakeIResourceManager;
import net.minecraft.client.resources.IResourceManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IResourceManager.class)
public interface MixinIResourceManager extends IFakeIResourceManager {

}
