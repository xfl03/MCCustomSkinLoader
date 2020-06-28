package customskinloader.mixin;

import customskinloader.fake.FakeClientPlayer;
import net.minecraft.client.gui.spectator.PlayerMenuObject;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerMenuObject.class)
public abstract class MixinPlayerMenuObject {
    @Redirect(
        method = "Lnet/minecraft/client/gui/spectator/PlayerMenuObject;<init>(Lcom/mojang/authlib/GameProfile;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getLocationSkin(Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;"
        )
    )
    private ResourceLocation redirect_init(String username) {
        return FakeClientPlayer.getLocationSkin(username);
    }

    @Redirect(
        method = "Lnet/minecraft/client/gui/spectator/PlayerMenuObject;<init>(Lcom/mojang/authlib/GameProfile;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getDownloadImageSkin(Lnet/minecraft/util/ResourceLocation;Ljava/lang/String;)Lnet/minecraft/client/renderer/ThreadDownloadImageData;"
        )
    )
    private ThreadDownloadImageData redirect_init(ResourceLocation resourceLocationIn, String username) {
        return FakeClientPlayer.getDownloadImageSkin(resourceLocationIn, username);
    }
}
