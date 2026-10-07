package customskinloader.test.agent.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MC145102FixTransformer implements ClassFileTransformer {
    private String version;

    public MC145102FixTransformer() {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("version.json");
             InputStreamReader isr = new InputStreamReader(is)) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[4096];
            int len;
            while ((len = isr.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }

            Pattern pattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(sb.toString());
            if (matcher.find()) {
                this.version = matcher.group(1);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read version information from version.json", e);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if ("net/minecraft/class_310".equals(className)) {
            return transformMinecraft(loader, classfileBuffer, targetFor(this.version, false));
        } else if ("net/minecraft/client/Minecraft".equals(className)) {
            return transformMinecraft(loader, classfileBuffer, targetFor(this.version, true));
        } else if ("net/minecraft/class_425".equals(className) && !this.version.startsWith("1.14")) {
            return transformLoadingOverlay(loader, classfileBuffer, "field_17771");
        } else if ("net/minecraft/client/gui/ResourceLoadProgressGui".equals(className)) {
            return transformLoadingOverlay(loader, classfileBuffer, "field_212979_g");
        }
        return null;
    }

    private static byte[] transformMinecraft(ClassLoader loader, byte[] classfileBuffer, Target target) {
        ClassNode classNode = new ClassNode();
        new ClassReader(classfileBuffer).accept(classNode, ClassReader.EXPAND_FRAMES);

        MethodNode enclosing = null;
        for (MethodNode method : classNode.methods) {
            if (containsTitleScreen(method, target) && findLambdaCall(method, target.l1Name, target.l1Desc) != null) {
                enclosing = method;
                break;
            }
        }
        if (enclosing == null) {
            throw new IllegalStateException("no method builds a " + target.titleScreen + " and creates " + target.l1Name + target.l1Desc);
        }

        ScreenBlock block = locateScreenBlock(enclosing, target);
        InsnList moved = extract(enclosing, block);

        MethodNode lambda = findMethod(classNode, target.l1Name, target.l1Desc);
        if (lambda == null) {
            throw new IllegalStateException("splash callback lambda " + target.l1Name + target.l1Desc + " not found");
        }

        int sSlot = -1;
        int iSlot = -1;
        if (!block.fieldBased) {
            // 1.15+: thread the auto connect server name/port through the lambda chain as captures
            InvokeDynamicInsnNode call = findLambdaCall(enclosing, target.l1Name, target.l1Desc);
            int[] slots = addCaptures(enclosing, call, lambda, block.sSlot, block.iSlot);
            if (target.l2Name != null) {
                MethodNode inner = findMethod(classNode, target.l2Name, target.l2Desc);
                if (inner == null) {
                    throw new IllegalStateException("nested lambda " + target.l2Name + target.l2Desc + " not found");
                }
                InvokeDynamicInsnNode innerCall = findLambdaCall(lambda, target.l2Name, target.l2Desc);
                if (innerCall == null) {
                    throw new IllegalStateException("call site of " + target.l2Name + target.l2Desc + " not found in " + lambda.name + lambda.desc);
                }
                int[] innerSlots = addCaptures(lambda, innerCall, inner, slots[0], slots[1]);
                lambda = inner;
                sSlot = innerSlots[0];
                iSlot = innerSlots[1];
            } else {
                sSlot = slots[0];
                iSlot = slots[1];
            }
        }

        for (AbstractInsnNode insn : moved) {
            if (insn instanceof VarInsnNode) {
                VarInsnNode var = (VarInsnNode) insn;
                if (var.var == block.sSlot && !block.fieldBased) {
                    var.var = sSlot;
                } else if (var.var == block.iSlot && !block.fieldBased) {
                    var.var = iSlot;
                }
            }
        }
        insertBeforeReturn(lambda, moved);

        ClassWriter writer = createClassWriter(loader);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] transformLoadingOverlay(ClassLoader loader, byte[] classfileBuffer, String fieldName) {
        ClassNode classNode = new ClassNode();
        new ClassReader(classfileBuffer).accept(classNode, ClassReader.EXPAND_FRAMES);

        for (MethodNode method : classNode.methods) {
            if (method.tryCatchBlocks == null || method.tryCatchBlocks.isEmpty()) {
                continue;
            }
            TryCatchBlockNode tryCatch = method.tryCatchBlocks.get(0);
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                // Locate: this.fadeOutStart = Util.getMillis()
                if (insn.getOpcode() != Opcodes.PUTFIELD) {
                    continue;
                }
                FieldInsnNode field = (FieldInsnNode) insn;
                if (!/*"field_17771"*/fieldName.equals(field.name) || !"J".equals(field.desc)) {
                    continue;
                }
                AbstractInsnNode call = insn.getPrevious();  // INVOKESTATIC Util.getMillis()
                AbstractInsnNode self = call.getPrevious();  // ALOAD 0

                // Move the three instructions in front of the try block; newStart ends up right after
                // getMillis(), so the try still begins at reload.checkExceptions(). Setting the timestamp
                // before the callback keeps a re-entrant render (fired from the callback) from running it twice.
                InsnList moved = new InsnList();
                method.instructions.remove(self);
                method.instructions.remove(call);
                method.instructions.remove(insn);
                moved.add(self);
                moved.add(call);
                moved.add(insn);

                LabelNode start = tryCatch.start;
                LabelNode newStart = new LabelNode();
                method.instructions.insert(start, newStart);
                method.instructions.insertBefore(newStart, moved);
                tryCatch.start = newStart;
                break;
            }
        }

        ClassWriter writer = createClassWriter(loader);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    /**
     * Intermediary names of the two lambdas involved, per Minecraft version:
     * {@code titleScreen} = TitleScreen/MainMenuScreen, {@code l1} = the lambda handed to the splash
     * overlay, {@code l2} = the lambda nested inside l1 that the initial screen code is moved into
     * (null when the code goes directly into l1 as in 1.14).
     */
    static Target targetFor(String mcVersion, boolean isForge) {
        if (mcVersion.startsWith("1.14")) {
            return new Target(isForge ? "net/minecraft/client/gui/screen/MainMenuScreen" : "net/minecraft/class_442", isForge ? "lambda$init$3" : "method_18504", "()V", null, null);
        } else if (mcVersion.startsWith("1.15")) {
            return new Target("net/minecraft/class_442", "method_24040", "(Ljava/util/List;Ljava/util/Optional;)V", "method_24227", "(Ljava/util/List;)V");
        } else if (mcVersion.startsWith("1.16") || mcVersion.startsWith("1.17") || mcVersion.startsWith("1.18") || mcVersion.startsWith("1.19")) {
            return new Target("net/minecraft/class_442", "method_24040", "(Ljava/util/Optional;)V", "method_29338", "()V");
        }
        return null;
    }

    private static boolean containsTitleScreen(MethodNode method, Target target) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() == Opcodes.NEW && target.titleScreen.equals(((TypeInsnNode) insn).desc)) {
                return true;
            }
        }
        return false;
    }

    private static InvokeDynamicInsnNode findLambdaCall(MethodNode method, String name, String desc) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode call = (InvokeDynamicInsnNode) insn;
                if (call.bsmArgs.length >= 2 && call.bsmArgs[1] instanceof Handle) {
                    Handle handle = (Handle) call.bsmArgs[1];
                    if (handle.getName().equals(name) && handle.getDesc().equals(desc)) {
                        return call;
                    }
                }
            }
        }
        return null;
    }

    private static ScreenBlock locateScreenBlock(MethodNode method, Target target) {
        InsnList insns = method.instructions;
        int titleIndex = -1;
        for (int i = 0; i < insns.size(); i++) {
            AbstractInsnNode insn = insns.get(i);
            if (insn.getOpcode() == Opcodes.NEW && target.titleScreen.equals(((TypeInsnNode) insn).desc)) {
                titleIndex = i;
                break;
            }
        }
        if (titleIndex < 0) {
            throw new IllegalStateException("no NEW " + target.titleScreen + " in " + method.name + method.desc);
        }

        int guardIndex = -1;
        for (int i = titleIndex - 1; i >= 0; i--) {
            if (insns.get(i).getOpcode() == Opcodes.IFNULL) {
                guardIndex = i;
                break;
            }
        }
        if (guardIndex < 1) {
            throw new IllegalStateException("no null check guarding the initial screen block");
        }

        // 1.14 reads Minecraft.serverName/serverPort from fields, 1.15+ from constructor locals
        boolean fieldBased = insns.get(guardIndex - 1).getOpcode() == Opcodes.GETFIELD;
        int start = fieldBased ? guardIndex - 2 : guardIndex - 1;
        if (start < 0 || insns.get(start).getOpcode() != Opcodes.ALOAD) {
            throw new IllegalStateException("unexpected guard in the initial screen block");
        }

        ScreenBlock block = new ScreenBlock();
        block.fieldBased = fieldBased;
        block.start = start;
        block.sSlot = ((VarInsnNode) insns.get(start)).var;

        int elseIndex = insns.indexOf(((JumpInsnNode) insns.get(guardIndex)).label);
        if (elseIndex < 0) {
            throw new IllegalStateException("dangling else label in the initial screen block");
        }
        int gotoIndex = -1;
        for (int i = guardIndex + 1; i < elseIndex; i++) {
            if (insns.get(i).getOpcode() == Opcodes.GOTO) {
                gotoIndex = i;
                break;
            }
        }
        if (gotoIndex < 0) {
            throw new IllegalStateException("no goto at the end of the then branch");
        }
        int endIndex = insns.indexOf(((JumpInsnNode) insns.get(gotoIndex)).label);
        if (endIndex < elseIndex) {
            throw new IllegalStateException("unexpected end label of the initial screen block");
        }
        block.end = endIndex;

        Set<Integer> seenIload = new HashSet<>();
        for (int i = block.start; i <= block.end; i++) {
            AbstractInsnNode insn = insns.get(i);
            int opcode = insn.getOpcode();
            if (opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD) {
                int var = ((VarInsnNode) insn).var;
                if (var != 0 && var != block.sSlot && opcode == Opcodes.ILOAD) {
                    seenIload.add(var);
                } else if (var != 0 && var != block.sSlot) {
                    throw new IllegalStateException("unexpected local " + var + " (opcode " + opcode + ") in the initial screen block");
                }
            }
        }
        if (!fieldBased) {
            if (seenIload.size() != 1) {
                throw new IllegalStateException("expected exactly one server port local, found " + seenIload);
            }
            block.iSlot = seenIload.iterator().next();
        }
        return block;
    }

    /** Cut [block.start, block.end] out of the enclosing method and hand the instructions over. */
    private static InsnList extract(MethodNode method, ScreenBlock block) {
        InsnList insns = method.instructions;
        List<AbstractInsnNode> nodes = new ArrayList<>();
        for (int i = block.start; i <= block.end; i++) {
            nodes.add(insns.get(i));
        }

        for (int i = block.end; i >= block.start; i--) {
            insns.remove(insns.get(i));
        }
        InsnList moved = new InsnList();
        for (AbstractInsnNode node : nodes) {
            moved.add(node);
        }
        return moved;
    }

    private static MethodNode findMethod(ClassNode classNode, String name, String desc) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }
        return null;
    }

    private static final Type STRING = Type.getObjectType("java/lang/String");
    private static final Type INT = Type.INT_TYPE;
    /**
     * Adds a {@code (String, int)} capture to the lambda {@code lambda} and its call site {@code call}
     * (which lives in {@code owner}), and pushes the values at the call site. Returns the slots the
     * captured values occupy inside the lambda body.
     */
    private static int[] addCaptures(MethodNode owner, InvokeDynamicInsnNode call, MethodNode lambda, int sSlot, int iSlot) {
        Handle impl = (Handle) call.bsmArgs[1];
        Type instantiated = (Type) call.bsmArgs[2];
        int samArgs = instantiated.getArgumentTypes().length;

        Type[] implArgs = Type.getArgumentTypes(impl.getDesc());
        int captured = implArgs.length - samArgs;
        Type[] newImplArgs = insert(implArgs, captured, new Type[]{STRING, INT});
        String newImplDesc = Type.getMethodDescriptor(Type.getReturnType(impl.getDesc()), newImplArgs);
        call.bsmArgs[1] = new Handle(impl.getTag(), impl.getOwner(), impl.getName(), newImplDesc, impl.isInterface());

        Type[] indyArgs = Type.getArgumentTypes(call.desc);
        Type[] newIndyArgs = append(indyArgs, new Type[]{STRING, INT});
        call.desc = Type.getMethodDescriptor(Type.getReturnType(call.desc), newIndyArgs);

        lambda.desc = newImplDesc;

        boolean isStatic = (lambda.access & Opcodes.ACC_STATIC) != 0;
        int firstSamSlot = parameterSlot(impl.getDesc(), isStatic, captured);
        // the captures are inserted in front of the SAM parameters, so everything that reads those
        // parameters (or any local allocated after them) moves up by two slots
        shiftLocalSlots(lambda, firstSamSlot, STRING.getSize() + INT.getSize());
        int newS = parameterSlot(newImplDesc, isStatic, captured);
        int newI = parameterSlot(newImplDesc, isStatic, captured + 1);

        owner.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, sSlot));
        owner.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, iSlot));
        return new int[]{newS, newI};
    }

    private static int parameterSlot(String desc, boolean isStatic, int index) {
        int slot = isStatic ? 0 : 1;
        Type[] args = Type.getArgumentTypes(desc);
        for (int i = 0; i < index; i++) {
            slot += args[i].getSize();
        }
        return slot;
    }

    /** Moves every local variable at or above {@code fromSlot} up by {@code delta} slots. */
    private static void shiftLocalSlots(MethodNode method, int fromSlot, int delta) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof VarInsnNode) {
                VarInsnNode var = (VarInsnNode) insn;
                if (var.var >= fromSlot) {
                    var.var += delta;
                }
            } else if (insn instanceof IincInsnNode) {
                IincInsnNode inc = (IincInsnNode) insn;
                if (inc.var >= fromSlot) {
                    inc.var += delta;
                }
            }
        }
    }

    private static Type[] insert(Type[] array, int index, Type[] values) {
        Type[] result = new Type[array.length + values.length];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(values, 0, result, index, values.length);
        System.arraycopy(array, index, result, index + values.length, array.length - index);
        return result;
    }

    private static Type[] append(Type[] array, Type[] values) {
        return insert(array, array.length, values);
    }

    private static void insertBeforeReturn(MethodNode method, InsnList moved) {
        AbstractInsnNode ret = null;
        for (AbstractInsnNode insn = method.instructions.getLast(); insn != null; insn = insn.getPrevious()) {
            if (insn.getOpcode() == Opcodes.RETURN) {
                ret = insn;
                break;
            }
        }
        if (ret == null) {
            throw new IllegalStateException("lambda " + method.name + method.desc + " has no RETURN");
        }
        method.instructions.insertBefore(ret, moved);
    }

    private static ClassWriter createClassWriter(ClassLoader loader) {
        return new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                try {
                    Class<?> class1 = Class.forName(type1.replace('/', '.'), false, loader);
                    Class<?> class2 = Class.forName(type2.replace('/', '.'), false, loader);
                    if (class1.isAssignableFrom(class2)) {
                        return type1;
                    }
                    if (class2.isAssignableFrom(class1)) {
                        return type2;
                    }
                    if (class1.isInterface() || class2.isInterface()) {
                        return "java/lang/Object";
                    }
                    do {
                        class1 = class1.getSuperclass();
                    } while (!class1.isAssignableFrom(class2));
                    return class1.getName().replace('.', '/');
                } catch (Throwable t) {
                    return "java/lang/Object";
                }
            }
        };
    }

    /** Names of the class/lambdas to patch, in whatever namespace the class is loaded in. */
    final static class Target {
        /** TitleScreen/MainMenuScreen class (the class whose instantiation marks the initial screen block). */
        final String titleScreen;
        /** Lambda passed to the resource reload splash overlay. */
        final String l1Name;
        final String l1Desc;
        /** Lambda nested inside {@link #l1Name} that receives the moved code, or null. */
        final String l2Name;
        final String l2Desc;

        Target(String titleScreen, String l1Name, String l1Desc, String l2Name, String l2Desc) {
            this.titleScreen = titleScreen;
            this.l1Name = l1Name;
            this.l1Desc = l1Desc;
            this.l2Name = l2Name;
            this.l2Desc = l2Desc;
        }
    }

    /** The initial screen block: guard, then branch, goto, else branch. */
    private final static class ScreenBlock {
        int start;
        int end;
        boolean fieldBased;
        int sSlot;
        int iSlot;
    }
}
