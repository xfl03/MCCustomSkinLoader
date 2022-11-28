package customskinloader.mixin;

import java.util.Map;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.fake.FakeSkinManager;
import net.minecraft.client.resources.SkinManager$3;
import net.minecraft.client.resources.SkinManager$3$1;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// For 1.12.2-
@Mixin(SkinManager$3$1.class)
public abstract class MixinSkinManager$3$1 {
    @Final
    @Shadow
    Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> field_152803_a;

    @Final
    @Shadow
    SkinManager$3 field_152804_b;

    @Inject(
        method = "Lnet/minecraft/client/resources/SkinManager$3$1;run()V",
        at = @At("RETURN")
    )
    private void inject_run(CallbackInfo callbackInfo) {
        FakeSkinManager.loadElytraTexture(this.field_152804_b.field_152802_d, this.field_152803_a, this.field_152804_b.field_152801_c);
    }
}
