package customskinloader.mixin;

import java.nio.file.Path;

import customskinloader.fake.FakeSkinBuffer;
import customskinloader.fake.texture.FakeThreadDownloadImageData;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@SuppressWarnings("target")
public abstract class MixinSkinTextureDownloader {
    // 24w46a ~ 25w44a
    @Mixin(SkinTextureDownloader.class)
    public abstract static class V1 {
        @ModifyArgs(
            method = "Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;downloadAndRegisterSkin(Lnet/minecraft/util/ResourceLocation;Ljava/nio/file/Path;Ljava/lang/String;Z)Ljava/util/concurrent/CompletableFuture;",
            at = @At(
                value = "INVOKE",
                target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;",
                remap = false
            )
        )
        private static void modifyArg_downloadAndRegisterSkin(Args args, ResourceLocation location, Path path, String url, boolean bl) {
            args.set(0, FakeThreadDownloadImageData.createTextureV1(args.get(0), location, bl));
        }
    }

    // 24w46a+
    @Mixin(SkinTextureDownloader.class)
    public abstract static class V2 {
        @Redirect(
            method = {
                "Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;lambda$downloadAndRegisterSkin$0(Ljava/nio/file/Path;Ljava/lang/String;Z)Lnet/minecraft/client/renderer/texture/NativeImage;", // 24w46a ~ 25w37a
                "Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;lambda$downloadAndRegisterSkin$0(Ljava/nio/file/Path;Lnet/minecraft/core/ClientAsset$DownloadedTexture;Z)Lnet/minecraft/client/renderer/texture/NativeImage;" // 1.21.9-pre1+
            },
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;processLegacySkin(Lnet/minecraft/client/renderer/texture/NativeImage;Ljava/lang/String;)Lnet/minecraft/client/renderer/texture/NativeImage;"
            )
        )
        private static NativeImage redirect_lambda$downloadAndRegisterSkin$0(NativeImage image, String url) {
            return FakeSkinBuffer.processLegacySkin(image, url);
        }
    }

    // 25w45a+
    @Mixin(SkinTextureDownloader.class)
    public abstract static class V3 {
        @ModifyArgs(
            method = "Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;downloadAndRegisterSkin(Lnet/minecraft/resources/Identifier;Ljava/nio/file/Path;Ljava/lang/String;Z)Ljava/util/concurrent/CompletableFuture;",
            at = @At(
                value = "INVOKE",
                target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;",
                remap = false
            )
        )
        private static void modifyArg_downloadAndRegisterSkin(Args args, Identifier identifier, Path path, String url, boolean bl) {
            args.set(0, FakeThreadDownloadImageData.createTextureV2(args.get(0), identifier, bl));
        }
    }
}
