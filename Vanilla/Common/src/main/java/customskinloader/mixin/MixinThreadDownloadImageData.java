package customskinloader.mixin;

import customskinloader.fake.FakeSkinBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.NativeImage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("target")
public abstract class MixinThreadDownloadImageData {
    @Mixin(ThreadDownloadImageData.class) // 19w38a ~ 1.17-rc1
    public abstract static class V1 {
        @Final
        @Shadow
        private Runnable processTask;

        @Shadow
        private static NativeImage processLegacySkin(NativeImage nativeImageIn) {
            return null;
        }

        @Redirect(
            method = "Lnet/minecraft/client/renderer/ThreadDownloadImageData;loadTexture(Ljava/io/InputStream;)Lnet/minecraft/client/renderer/texture/NativeImage;",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/ThreadDownloadImageData;processLegacySkin(Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;"
            )
        )
        private NativeImage redirect_loadTexture(NativeImage image) {
            return FakeSkinBuffer.processLegacySkin(image, this.processTask, MixinThreadDownloadImageData.V1::processLegacySkin);
        }
    }

    @Mixin(ThreadDownloadImageData.class) // 1.17-rc2+
    public abstract static class V2 {
        @Final
        @Shadow
        private Runnable processTask;

        @Shadow
        abstract NativeImage processLegacySkin(NativeImage nativeImageIn);

        @Redirect(
            method = "Lnet/minecraft/client/renderer/ThreadDownloadImageData;loadTexture(Ljava/io/InputStream;)Lnet/minecraft/client/renderer/texture/NativeImage;",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/ThreadDownloadImageData;processLegacySkin(Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;"
            )
        )
        private NativeImage redirect_loadTexture(ThreadDownloadImageData _this, NativeImage image) {
            return FakeSkinBuffer.processLegacySkin(image, this.processTask, this::processLegacySkin);
        }
    }
}
