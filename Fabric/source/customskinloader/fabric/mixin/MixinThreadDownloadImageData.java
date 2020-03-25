package customskinloader.fabric.mixin;

import customskinloader.fake.FakeSkinBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.NativeImage;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThreadDownloadImageData.class)
public abstract class MixinThreadDownloadImageData {
    @Dynamic
    @Inject(
        method = "Lnet/minecraft/client/renderer/ThreadDownloadImageData;processLegacySkin(Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void inject_processLegacySkin(NativeImage image, CallbackInfoReturnable<NativeImage> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(FakeSkinBuffer.processLegacySkin(image));
    }
}
