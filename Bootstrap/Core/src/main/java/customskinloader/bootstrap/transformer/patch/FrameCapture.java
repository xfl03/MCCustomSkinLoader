package customskinloader.bootstrap.transformer.patch;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Computes the operand stack / local variable state around a given instruction without loading any
 * class. Only usable when the method already carries expanded (F_NEW) stack map frames, which is
 * the case for the byte array based transformation paths. Platforms that recompute frames (e.g.
 * ModLauncher) hand over compressed frames; callers must then fall back to letting the platform
 * compute the frames.
 */
final class FrameCapture {
    final Object[] localsBefore;
    final Object[] stackBefore;
    final Object[] localsAfter;
    final Object[] stackAfter;

    private FrameCapture(Object[] localsBefore, Object[] stackBefore, Object[] localsAfter, Object[] stackAfter) {
        this.localsBefore = localsBefore;
        this.stackBefore = stackBefore;
        this.localsAfter = localsAfter;
        this.stackAfter = stackAfter;
    }

    static FrameCapture capture(String owner, MethodNode methodNode, AbstractInsnNode target) {
        if (!hasExpandedFrames(methodNode)) {
            return null;
        }

        AnalyzerAdapter analyzer = new AnalyzerAdapter(owner, methodNode.access, methodNode.name, methodNode.desc, null);
        for (AbstractInsnNode instruction = methodNode.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
            if (instruction == target) {
                Object[] localsBefore = toFrameTypes(analyzer.locals);
                Object[] stackBefore = toFrameTypes(analyzer.stack);
                if (localsBefore == null || stackBefore == null) {
                    return null;
                }

                instruction.accept(analyzer);

                Object[] localsAfter = toFrameTypes(analyzer.locals);
                Object[] stackAfter = toFrameTypes(analyzer.stack);
                if (localsAfter == null || stackAfter == null) {
                    return null;
                }

                return new FrameCapture(localsBefore, stackBefore, localsAfter, stackAfter);
            }

            instruction.accept(analyzer);
        }
        return null;
    }

    private static boolean hasExpandedFrames(MethodNode methodNode) {
        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (instruction instanceof FrameNode && ((FrameNode) instruction).type != Opcodes.F_NEW) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts the slot based lists maintained by {@link AnalyzerAdapter} into the expanded frame
     * format expected by {@link FrameNode}. Long and double values occupy two slots but only one
     * frame entry, so the implicit {@code TOP} following them must be dropped.
     */
    private static Object[] toFrameTypes(List<Object> slots) {
        if (slots == null) {
            return null;
        }

        List<Object> types = new ArrayList<>(slots.size());
        for (int index = 0; index < slots.size(); index++) {
            Object slot = slots.get(index);
            if (!(slot instanceof String) && !(slot instanceof Integer)) {
                // Uninitialized types are represented by analyzer owned labels that cannot be mapped
                // back to labels of the method being transformed.
                return null;
            }

            types.add(slot);
            if (slot instanceof Integer && (slot.equals(Opcodes.LONG) || slot.equals(Opcodes.DOUBLE))) {
                index++;
            }
        }
        return types.toArray();
    }
}
