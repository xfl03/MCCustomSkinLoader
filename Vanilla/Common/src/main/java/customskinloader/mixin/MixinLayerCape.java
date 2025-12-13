package customskinloader.mixin;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("target")
public abstract class MixinLayerCape {
    // 19w39a ~ 25w42a
    @Mixin(
        value = LayerCape.class,
        priority = 990
    )
    public abstract static class V1 {
        @Redirect(
            method = {
                "Lnet/minecraft/client/renderer/entity/layers/LayerCape;doRenderLayer(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/AbstractClientPlayer;FFFFFFF)V", // 19w39a ~ 19w44a
                "Lnet/minecraft/client/renderer/entity/layers/LayerCape;doRenderLayer(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/AbstractClientPlayer;FFFFFF)V", // 19w45a ~ 1.21.1
                "Lnet/minecraft/client/renderer/entity/layers/LayerCape;doRenderLayer(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", // 24w33a ~ 1.21.8
                "Lnet/minecraft/client/renderer/entity/layers/LayerCape;doRenderLayer(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V" // 25w31a ~ 25w42a
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

    // 25w43a+
    @Mixin(
        value = LayerCape.class,
        priority = 990
    )
    public abstract static class V2 {
        @Redirect(
            method = "Lnet/minecraft/client/renderer/entity/layers/LayerCape;doRenderLayer(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", // 25w43a+
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entitySolid(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            ),
            require = 0
        )
        private net.minecraft.client.renderer.rendertype.RenderType redirect_doRenderLayer(Identifier identifier) {
            return RenderTypes.entityTranslucent(identifier);
        }
    }
}
