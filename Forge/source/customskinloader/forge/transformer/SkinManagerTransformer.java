package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SkinManagerTransformer {
    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.resources.SkinManager",
        methodNameSrg = "<init>",
        desc = "(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V"
    )
    public static class InitTransformer implements TransformerManager.IMethodTransformer {
        @Override
        public MethodNode transform(ClassNode cn, MethodNode mn) {
            cn.fields.removeIf(fn -> fn.name.equals("fakeManager") && fn.desc.equals("Lcustomskinloader/fake/FakeSkinManager;"));
            cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;", null, null));

            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain.getOpcode() == Opcodes.RETURN) {
                    InsnList il = new InsnList();
                    il.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    il.add(new TypeInsnNode(Opcodes.NEW, "customskinloader/fake/FakeSkinManager"));
                    il.add(new InsnNode(Opcodes.DUP));
                    il.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    il.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    il.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "customskinloader/fake/FakeSkinManager", "<init>", "(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V", false));
                    il.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
                    mn.instructions.insert(il);
                }
            }
            return mn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.resources.SkinManager",
        methodNameSrg = "func_152789_a",
        methodNames = "loadSkin",
        desc = "(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;"
    )
    public static class LoadSkinTransformer implements TransformerManager.IMethodTransformer {
        @Override
        public MethodNode transform(ClassNode cn, MethodNode mn) {
            InsnList il = new InsnList();
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
            il.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            il.add(new VarInsnNode(Opcodes.ALOAD, 1));
            il.add(new VarInsnNode(Opcodes.ALOAD, 2));
            il.add(new VarInsnNode(Opcodes.ALOAD, 3));
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "customskinloader/fake/FakeSkinManager", "loadSkin", "(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;", false));
            il.add(new InsnNode(Opcodes.ARETURN));
            il.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            mn.instructions.insert(il);
            return mn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.resources.SkinManager",
        methodNameSrg = "func_152790_a",
        methodNames = "loadProfileTextures",
        desc = "(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V"
    )
    public static class LoadProfileTexturesTransformer implements TransformerManager.IMethodTransformer {
        @Override
        public MethodNode transform(ClassNode cn, MethodNode mn) {
            InsnList il = new InsnList();
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
            il.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            il.add(new VarInsnNode(Opcodes.ALOAD, 1));
            il.add(new VarInsnNode(Opcodes.ALOAD, 2));
            il.add(new VarInsnNode(Opcodes.ILOAD, 3));
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "customskinloader/fake/FakeSkinManager", "loadProfileTextures", "(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V", false));
            il.add(new InsnNode(Opcodes.RETURN));
            il.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            mn.instructions.insert(il);
            return mn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.resources.SkinManager",
        methodNameSrg = "func_152788_a",
        methodNames = "loadSkinFromCache",
        desc = "(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;"
    )
    public static class LoadSkinFromCacheTransformer implements TransformerManager.IMethodTransformer {
        @Override
        public MethodNode transform(ClassNode cn, MethodNode mn) {
            InsnList il = new InsnList();
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
            il.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            il.add(new VarInsnNode(Opcodes.ALOAD, 1));
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "customskinloader/fake/FakeSkinManager", "loadSkinFromCache", "(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;", false));
            il.add(new InsnNode(Opcodes.ARETURN));
            il.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            mn.instructions.insert(il);
            return mn;
        }
    }
}
