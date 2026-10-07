package customskinloader.test.agent.impl;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class AuthlibPrivilegesFixTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if ("com/mojang/authlib/HttpAuthenticationService".equals(className)) {
            ClassNode classNode = new ClassNode();
            new ClassReader(classfileBuffer).accept(classNode, ClassReader.EXPAND_FRAMES);

            for (MethodNode methodNode : classNode.methods) {
                if ("performGetRequest".equals(methodNode.name) && "(Ljava/net/URL;Ljava/lang/String;)Ljava/lang/String;".equals(methodNode.desc)) {
                    AbstractInsnNode first = null;
                    for (AbstractInsnNode insn = methodNode.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL && "getErrorStream".equals(((MethodInsnNode) insn).name)) {
                            first = insn.getNext().getNext(); // skip the ASTORE of the error stream
                        }
                    }

                    AbstractInsnNode last = null;
                    for (AbstractInsnNode insn = first; insn != null; insn = insn.getNext()) {
                        if (insn.getOpcode() == Opcodes.ARETURN) {
                            last = insn;
                            break;
                        }
                    }

                    // The try/catch range of the handler ends inside the removed block, and the debug entries
                    // of locals declared there point into it, so both are redirected to the point the block
                    // is cut at.
                    List<LabelNode> droppedLabels = new ArrayList<>();
                    for (AbstractInsnNode insn = first; insn != last.getNext(); insn = insn.getNext()) {
                        if (insn instanceof LabelNode) {
                            droppedLabels.add((LabelNode) insn);
                        }
                    }
                    LabelNode anchor = new LabelNode();
                    methodNode.instructions.insertBefore(last.getNext(), anchor);
                    for (TryCatchBlockNode block : methodNode.tryCatchBlocks) {
                        if (droppedLabels.contains(block.start)) {
                            block.start = anchor;
                        }
                        if (droppedLabels.contains(block.end)) {
                            block.end = anchor;
                        }
                        if (droppedLabels.contains(block.handler)) {
                            block.handler = anchor;
                        }
                    }

                    List<AbstractInsnNode> removed = new ArrayList<>();
                    for (AbstractInsnNode insn = first; insn != null; insn = insn.getNext()) {
                        removed.add(insn);
                        if (insn == last) {
                            break;
                        }
                    }
                    for (AbstractInsnNode insn : removed) {
                        methodNode.instructions.remove(insn);
                    }
                }
            }

            ClassWriter writer = createClassWriter(loader);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        return null;
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
}
