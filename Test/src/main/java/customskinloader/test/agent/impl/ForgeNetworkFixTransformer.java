package customskinloader.test.agent.impl;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ForgeNetworkFixTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if ("net/minecraft/network/NetworkManager".equals(className)) {
            try {
                ClassReader reader = new ClassReader(classfileBuffer);
                ClassNode node = new ClassNode();
                reader.accept(node, 0);
                for (MethodNode method : node.methods) {
                    if ("func_150723_a".equals(method.name) && ("(Lnet/minecraft/network/EnumConnectionState;)V".equals(method.desc) /* 1.13.2 */ || "(Lnet/minecraft/network/ProtocolType;)V".equals(method.desc) /* 1.14.x */ )) {
                        for (AbstractInsnNode current = method.instructions.getFirst(); current != null; current = current.getNext()) {
                            if (current.getOpcode() == Opcodes.INVOKEINTERFACE) {
                                MethodInsnNode call = (MethodInsnNode) current;
                                if ("io/netty/channel/ChannelConfig".equals(call.owner) && "setAutoRead".equals(call.name) && "(Z)Lio/netty/channel/ChannelConfig;".equals(call.desc)) {
                                    InsnList rearm = new InsnList();
                                    rearm.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    rearm.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/NetworkManager", "field_150746_k", "Lio/netty/channel/Channel;"));
                                    rearm.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "io/netty/channel/Channel", "read", "()Lio/netty/channel/Channel;", true));
                                    rearm.add(new InsnNode(Opcodes.POP));
                                    method.instructions.insert(current.getNext(), rearm);

                                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                                    node.accept(writer);
                                    log("patched NetworkManager state setter to re-arm inbound reads");
                                    return writer.toByteArray();
                                }
                            }
                        }
                    }
                }
                log("NetworkManager state setter shape did not match; no changes applied");
            } catch (Throwable failure) {
                log("patch failed; class left unchanged: " + failure);
            }
        }
        return null;
    }

    private static void log(String message) {
        System.out.println("[forge-network-fix] " + message);
    }
}
