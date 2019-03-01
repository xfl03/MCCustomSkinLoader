package customskinloader.forge.transformer;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import customskinloader.forge.TransformerManager.IMethodTransformer;
import customskinloader.forge.TransformerManager.TransformTarget;

public class PlayerTabTransformer {
    @TransformTarget(className = "net.minecraft.client.gui.GuiPlayerTabOverlay",
            methodNames = {"func_175249_a", "renderPlayerlist"},
            desc = "(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V")
    public static class ScoreObjectiveTransformer implements IMethodTransformer {
        //From: http://git.oschina.net/AsteriskTeam/TabIconHackForge/blob/master/src/main/java/kengxxiao/tabiconhack/coremod/TabIconHackForgeClassTransformer.java#L30-L43
        @Override
        public void transform(ClassNode cn, MethodNode mn) {
            ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode node = iterator.next();
                if (node instanceof VarInsnNode) {
                    VarInsnNode varNode = (VarInsnNode) node;
                    if (varNode.getOpcode() == Opcodes.ISTORE && varNode.var == 11) {
                        mn.instructions.set(varNode.getPrevious(), new InsnNode(Opcodes.ICONST_1));
                    }
                }
            }
        }
    }
}