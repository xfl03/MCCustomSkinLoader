package customskinloader.forge.transformer;

import java.util.ListIterator;

import customskinloader.forge.TransformerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class PlayerTabTransformer {
    @TransformerManager.TransformTarget(className = "net.minecraft.client.gui.GuiPlayerTabOverlay",
            methodNameSrg = "func_175249_a",
            methodNames = "renderPlayerlist",
            desc = "(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V")
    public static class ScoreObjectiveTransformer implements TransformerManager.IMethodTransformer {
        //From: http://git.oschina.net/AsteriskTeam/TabIconHackForge/blob/master/src/main/java/kengxxiao/tabiconhack/coremod/TabIconHackForgeClassTransformer.java#L30-L43
        @Override
        public MethodNode transform(ClassNode cn, MethodNode mn) {
            for (ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator(); iterator.hasNext();) {
                AbstractInsnNode node = iterator.next();
                if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    MethodInsnNode min = (MethodInsnNode) node;
                    if (TransformerManager.checkClassName(min.owner, "net/minecraft/client/Minecraft") && TransformerManager.checkMethodName(min.owner, min.name, min.desc, "func_71387_A") && TransformerManager.checkMethodDesc(min.desc, "()Z")) {
                        mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP));
                        mn.instructions.set(node, new InsnNode(Opcodes.ICONST_1));
                    }
                }
            }
            return mn;
        }
    }
}