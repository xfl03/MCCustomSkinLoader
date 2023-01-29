package customskinloader.mixin;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    value = RenderPlayer.class,
    priority = 990
)
@SuppressWarnings("target")
public abstract class MixinRenderPlayer {
    @Redirect(
        method = {
            "Lnet/minecraft/client/renderer/entity/RenderPlayer;renderItem(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;Lnet/minecraft/client/entity/AbstractClientPlayer;Lnet/minecraft/client/model/ModelRenderer;Lnet/minecraft/client/model/ModelRenderer;)V", // 19w39a~19w44a
            "Lnet/minecraft/client/renderer/entity/RenderPlayer;renderItem(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/AbstractClientPlayer;Lnet/minecraft/client/model/ModelRenderer;Lnet/minecraft/client/model/ModelRenderer;)V" // 19w45a+
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType;getEntitySolid(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
        ),
        require = 0
    )
    private RenderType redirect_renderItem(ResourceLocation locationIn) {
        return RenderType.func_228644_e_(locationIn);
    }
}
