package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TileEntitySkullTransformer {
    @TransformerManager.TransformTarget(
        className = "net.minecraft.tileentity.TileEntitySkull",
        methodNames = {"updateGameProfile", "func_174884_b"},
        desc = "(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/GameProfile;"
    )
    public static class UpdateGameProfileTransformer implements TransformerManager.IMethodTransformer {
        @Override
        public void transform(ClassNode cn, MethodNode mn) {
            InsnList il = new InsnList();
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
            il.add(new InsnNode(Opcodes.ARETURN));
            mn.instructions.insert(il);
        }
    }
}
