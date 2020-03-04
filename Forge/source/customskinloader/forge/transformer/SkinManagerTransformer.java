package customskinloader.forge.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import customskinloader.forge.TransformerManager.IMethodTransformer;
import customskinloader.forge.TransformerManager.TransformTarget;

public class SkinManagerTransformer {
    @TransformTarget(className="net.minecraft.client.resources.SkinManager",
            methodNames={"<init>"},
            desc="(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V")
    public static class InitTransformer implements IMethodTransformer{
        @Override
        public void transform(ClassNode cn,MethodNode mn) {
            boolean hasField = false;
            for (FieldNode fn : cn.fields) {
                if (fn.name.equals("fakeManager")) {
                    hasField = true;
                    break;
                }
            }
            if (!hasField) {
                cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;", null, null));
            }
            mn.instructions.clear();
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V", false));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
            mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "customskinloader/fake/FakeSkinManager"));
            mn.instructions.add(new InsnNode(Opcodes.DUP));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,3));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"customskinloader/fake/FakeSkinManager","<init>",
                    "(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V", false));
            mn.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
        }
    }
    @TransformTarget(className="net.minecraft.client.resources.SkinManager",
            methodNames={"func_152789_a","loadSkin"},
            desc="(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;")
    public static class LoadSkinTransformer implements IMethodTransformer{
        @Override
        public void transform(ClassNode cn,MethodNode mn) {
            mn.instructions.clear();
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
            mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,3));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"customskinloader/fake/FakeSkinManager","loadSkin",
                    "(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;", false));
            mn.instructions.add(new InsnNode(Opcodes.ARETURN));
        }
    }
    @TransformTarget(className="net.minecraft.client.resources.SkinManager",
            methodNames={"func_152790_a","loadProfileTextures"},
            desc="(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V")
    public static class LoadProfileTexturesTransformer implements IMethodTransformer{
        @Override
        public void transform(ClassNode cn,MethodNode mn) {
            mn.instructions.clear();
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
            mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));
            mn.instructions.add(new VarInsnNode(Opcodes.ILOAD,3));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"customskinloader/fake/FakeSkinManager","loadProfileTextures",
                    "(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V", false));
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
        }
    }
    @TransformTarget(className="net.minecraft.client.resources.SkinManager",
            methodNames={"func_152788_a","loadSkinFromCache"},
            desc="(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;")
    public static class LoadSkinFromCacheTransformer implements IMethodTransformer{
        @Override
        public void transform(ClassNode cn,MethodNode mn) {
            mn.instructions.clear();
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
            mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"customskinloader/fake/FakeSkinManager","loadSkinFromCache",
                    "(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;", false));
            mn.instructions.add(new InsnNode(Opcodes.ARETURN));
        }
    }
}
