package customskinloader.bootstrap.transformer.patch;

import customskinloader.bootstrap.mapping.Mappings;
import customskinloader.bootstrap.transformer.ClassTransformationContext;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SkinManagerPatchTest implements Opcodes {
    private static final String CACHE_KEY = "net/minecraft/client/resources/SkinManager$CacheKey";
    private static final String SERVICES = "net/minecraft/server/Services";
    private static final String PROFILE_TEXTURES = "com/mojang/authlib/minecraft/MinecraftProfileTextures";
    private static final String SESSION_SERVICE = "com/mojang/authlib/minecraft/SessionService";
    private static final String PROPERTY = "com/mojang/authlib/properties/Property";

    @Test
    public void transformsMinecraft26_3SessionServiceInterfaceInvocation() {
        ClassNode classNode = new ClassNode();
        classNode.name = "net/minecraft/client/resources/SkinManager$1";
        classNode.methods.add(createLoadMethod());
        classNode.methods.add(createMinecraft26_3Lambda());

        ClassTransformationContext context = ClassTransformationContext.create(classNode.name, classNode, Mappings.EMPTY);
        assertTrue(new SkinManagerPatch().transform(context));

        MethodNode lambda = classNode.methods.get(1);
        assertFalse(containsInvocation(lambda, SESSION_SERVICE, "unpackTextures"));
        assertTrue(containsInvocation(lambda, "customskinloader/fake/FakeSkinManager", "loadSkinFromCache"));
    }

    private static MethodNode createLoadMethod() {
        MethodNode method = new MethodNode(ACC_PUBLIC, "load", "(L" + CACHE_KEY + ";)Ljava/util/concurrent/CompletableFuture;", null, null);
        method.instructions.add(new MethodInsnNode(INVOKESTATIC, "java/util/concurrent/CompletableFuture", "supplyAsync", "(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", false));
        method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/concurrent/CompletableFuture", "thenComposeAsync", "(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", false));
        method.instructions.add(new InsnNode(ARETURN));
        return method;
    }

    private static MethodNode createMinecraft26_3Lambda() {
        MethodNode method = new MethodNode(ACC_PRIVATE | ACC_STATIC, "lambda$load$0", "(L" + CACHE_KEY + ";L" + SERVICES + ";)L" + PROFILE_TEXTURES + ";", null, null);
        method.instructions.add(new MethodInsnNode(INVOKEINTERFACE, SESSION_SERVICE, "unpackTextures", "(L" + PROPERTY + ";)L" + PROFILE_TEXTURES + ";", true));
        method.instructions.add(new InsnNode(ARETURN));
        return method;
    }

    private static boolean containsInvocation(MethodNode method, String owner, String name) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode invocation = (MethodInsnNode) instruction;
                if (owner.equals(invocation.owner) && name.equals(invocation.name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
