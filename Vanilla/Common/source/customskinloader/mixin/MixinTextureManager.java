package customskinloader.mixin;

import customskinloader.fake.itf.IFakeTextureManager;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Implements({
    @Interface(
        iface = IFakeTextureManager.V1.class,
        prefix = "fake1$"
    ),
    @Interface(
        iface = IFakeTextureManager.V2.class,
        prefix = "fake2$"
    ),
})
@Mixin(TextureManager.class)
public abstract class MixinTextureManager {

}
