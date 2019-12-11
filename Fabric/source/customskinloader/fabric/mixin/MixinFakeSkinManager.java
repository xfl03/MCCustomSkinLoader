package customskinloader.fabric.mixin;

import customskinloader.fake.FakeSkinManager;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @see customskinloader.fabric.MixinConfigPlugin#preApply
 */
@Mixin(FakeSkinManager.class)
public abstract class MixinFakeSkinManager {
}
