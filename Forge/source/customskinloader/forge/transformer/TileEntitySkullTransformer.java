package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TileEntitySkullTransformer {
    @TransformerManager.TransformTarget(
        className = "net.minecraft.tileentity.TileEntitySkull",
        methodNameSrg = "func_174884_b",
        methodNames = {"updateGameprofile", "updateGameProfile"},
        desc = "(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/GameProfile;"
    )
    public static class UpdateGameProfileTransformer implements TransformerManager.IMethodTransformer {
        @Override
        public MethodNode transform(ClassNode cn, MethodNode mn) {
            InsnList il = new InsnList();
            LabelNode ln = new LabelNode();
            il.add(new FieldInsnNode(Opcodes.GETSTATIC, "customskinloader/CustomSkinLoader", "config", "Lcustomskinloader/config/Config;"));
            il.add(new FieldInsnNode(Opcodes.GETFIELD, "customskinloader/config/Config", "forceFillSkullNBT", "Z"));
            il.add(new JumpInsnNode(Opcodes.IFNE, ln));
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
            il.add(new InsnNode(Opcodes.ARETURN));
            il.add(ln);
            il.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            mn.instructions.insert(il);
            return mn;
        }
    }
}
