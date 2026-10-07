package customskinloader.test.agent.impl;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class NeoForgeVanillaFilterFixTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if ("net/neoforged/neoforge/network/filters/NetworkFilters".equals(className)) {
            try {
                ClassNode node = new ClassNode();
                new ClassReader(classfileBuffer).accept(node, 0);
                boolean changed = false;
                for (MethodNode method : node.methods) {
                    if (method.name.equals("injectIfNecessary") && method.desc.equals("(Lnet/minecraft/network/Connection;)V")) {
                        method.access |= Opcodes.ACC_SYNCHRONIZED;
                        changed = true;
                    }
                }
                if (!changed) {
                    log("injectIfNecessary not found or already synchronized; no changes applied");
                    return null;
                }

                ClassWriter writer = new ClassWriter(0);
                node.accept(writer);
                log("marked NetworkFilters.injectIfNecessary as synchronized");
                return writer.toByteArray();
            } catch (Throwable failure) {
                log("patch failed; class left unchanged: " + failure);
            }
        }
        return null;
    }

    private static void log(String message) {
        System.out.println("[neoforge-vanilla-filter-fix] " + message);
    }
}
