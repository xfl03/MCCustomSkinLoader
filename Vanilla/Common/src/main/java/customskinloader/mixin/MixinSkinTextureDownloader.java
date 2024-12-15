package customskinloader.mixin;

import customskinloader.fake.itf.FakeInterfaceManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SkinTextureDownloader.class)
@SuppressWarnings("target")
public abstract class MixinSkinTextureDownloader {
    @Redirect(
        method = "Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;lambda$registerTextureInManager$2(Lnet/minecraft/client/Minecraft;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/util/ResourceLocation;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureManager;loadTexture(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V"
        )
    )
    private static void redirect_lambda$registerTextureInManager$2(TextureManager manager, ResourceLocation location, AbstractTexture texture) {
        manager.registerAndLoad(location, (ReloadableTexture) FakeInterfaceManager.ResourceLocation_getTexture(location));
    }
}
