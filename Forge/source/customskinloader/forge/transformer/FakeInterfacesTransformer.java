package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class FakeInterfacesTransformer {
    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.Minecraft"
    )
    public static class MinecraftTransformer implements TransformerManager.IClassTransformer {
        @Override
        public void transform(ClassNode cn) {
            cn.interfaces.add("customskinloader/fake/itf/IFakeMinecraft");
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.renderer.texture.AbstractTexture"
    )
    public static class AbstractTextureTransfomer implements TransformerManager.IClassTransformer {
        @Override
        public void transform(ClassNode cn) {
            cn.interfaces.add("net/minecraft/client/renderer/texture/Texture");
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.renderer.texture.Texture"
    )
    public static class TextureTransformer implements TransformerManager.IClassTransformer {
        @Override
        public void transform(ClassNode cn) {
            if (cn.access == 0) {
                cn.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT;
            }
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.renderer.texture.TextureManager"
    )
    public static class TextureManagerTransformer implements TransformerManager.IClassTransformer {
        @Override
        public void transform(ClassNode cn) {
            cn.interfaces.add("customskinloader/fake/itf/IFakeTextureManager_1");
            cn.interfaces.add("customskinloader/fake/itf/IFakeTextureManager_2");
        }
    }
}
