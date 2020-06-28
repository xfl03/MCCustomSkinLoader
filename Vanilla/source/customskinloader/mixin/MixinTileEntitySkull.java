package customskinloader.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntitySkull.class)
public abstract class MixinTileEntitySkull {
    @Inject(
        method = "Lnet/minecraft/tileentity/TileEntitySkull;updateGameProfile(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/GameProfile;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void inject_updateGameProfile(GameProfile input, CallbackInfoReturnable<GameProfile> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(input);
    }
}
