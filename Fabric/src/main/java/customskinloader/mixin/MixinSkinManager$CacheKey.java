package customskinloader.mixin;

import customskinloader.fake.itf.IFakeSkinManagerCacheKey;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SkinManager.CacheKey.class)
public abstract class MixinSkinManager$CacheKey implements IFakeSkinManagerCacheKey {

}
