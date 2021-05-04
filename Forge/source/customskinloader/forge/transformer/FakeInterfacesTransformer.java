package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class FakeInterfacesTransformer {
    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.Minecraft"
    )
    public static class MinecraftTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            cn.interfaces.add("customskinloader/fake/itf/IFakeMinecraft");
            return cn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.renderer.ThreadDownloadImageData"
    )
    public static class ThreadDownloadImageDataTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            cn.interfaces.add("customskinloader/fake/itf/IFakeThreadDownloadImageData");

            MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, "resetNewBufferedImage", "(Ljava/awt/image/BufferedImage;)V", null, null);
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
            mn.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, TransformerManager.mapFieldName(cn.name, "field_110559_g", "Z"), "Z"));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            mn.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, TransformerManager.mapFieldName(cn.name, "field_110560_d", "Ljava/awt/image/BufferedImage;"), "Ljava/awt/image/BufferedImage;"));
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
            cn.methods.add(mn);

            return cn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.renderer.texture.AbstractTexture"
    )
    public static class AbstractTextureTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            cn.interfaces.add("net/minecraft/client/renderer/texture/Texture");
            return cn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.renderer.texture.Texture"
    )
    public static class TextureTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            if (cn.access == 0) {
                cn.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT;
            }
            return cn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.renderer.texture.TextureManager"
    )
    public static class TextureManagerTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            cn.interfaces.add("customskinloader/fake/itf/IFakeTextureManager_1");
            cn.interfaces.add("customskinloader/fake/itf/IFakeTextureManager_2");
            return cn;
        }
    }
}
