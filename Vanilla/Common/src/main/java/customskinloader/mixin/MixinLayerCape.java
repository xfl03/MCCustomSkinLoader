package customskinloader.mixin;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    value = LayerCape.class,
    priority = 990
)
@SuppressWarnings("target")
public abstract class MixinLayerCape {
    @Redirect(
        method = {
            "Lnet/minecraft/client/renderer/entity/layers/LayerCape;doRenderLayer(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/AbstractClientPlayer;FFFFFFF)V", // 19w39a~19w44a
            "Lnet/minecraft/client/renderer/entity/layers/LayerCape;doRenderLayer(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/AbstractClientPlayer;FFFFFF)V" // 19w45a+
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType;getEntitySolid(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
        ),
        require = 0
    )
    private RenderType redirect_doRenderLayer(ResourceLocation locationIn) {
        return RenderType.func_228644_e_(locationIn);
    }
}
