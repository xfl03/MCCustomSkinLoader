package customskinloader.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay {
    @Redirect(
        method = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;renderPlayerlist(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"
        )
    )
    private boolean modifyVariable_renderPlayerlist(Minecraft mc) {
        return true;
    }
}
