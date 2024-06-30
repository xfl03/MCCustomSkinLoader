package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
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
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain.getOpcode() == Opcodes.RETURN) {
                    InsnList il = new InsnList();
                    il.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "setSkinCacheDir", "(Ljava/io/File;)V", false));
                    mn.instructions.insertBefore(ain, il);
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
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain.getOpcode() == Opcodes.INVOKESPECIAL) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    if (TransformerManager.checkClassName(min.owner, "net/minecraft/client/renderer/ThreadDownloadImageData") && TransformerManager.checkMethodName(min.owner, min.name, min.desc, "<init>") && TransformerManager.checkMethodDesc(min.desc, "(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/IImageBuffer;)V")) {
                        InsnList il0 = new InsnList();
                        Type[] args = Type.getType(min.desc).getArgumentTypes();
                        StringBuilder sb = new StringBuilder("(");
                        for (int i = 0, len = args.length; i < len; i++) {
                            sb.append("Ljava/lang/Object;");
                        }
                        il0.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableList", "of", sb.append(")Lcom/google/common/collect/ImmutableList;").toString(), false));
                        il0.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        il0.add(new VarInsnNode(Opcodes.ALOAD, 2));

                        il0.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "createThreadDownloadImageData", "(Lcom/google/common/collect/ImmutableList;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;)[Ljava/lang/Object;", false));
                        for (int i = 0, len = args.length; i < len; i++) {
                            il0.add(new InsnNode(Opcodes.DUP));
                            il0.add(new IntInsnNode(Opcodes.BIPUSH, i));
                            il0.add(new InsnNode(Opcodes.AALOAD));
                            il0.add(new TypeInsnNode(Opcodes.CHECKCAST, args[i].getInternalName()));
                            il0.add(new InsnNode(Opcodes.SWAP));
                        }
                        il0.add(new InsnNode(Opcodes.POP));

                        mn.instructions.insertBefore(min, il0);

                        // This is only for 1.12.2-
                        InsnList il1 = new InsnList();
                        il1.add(new VarInsnNode(Opcodes.ALOAD, 3));
                        il1.add(new VarInsnNode(Opcodes.ALOAD, 4));
                        il1.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeClientPlayer", "putCache", "(Lnet/minecraft/client/renderer/ThreadDownloadImageData;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/ThreadDownloadImageData;", false));

                        mn.instructions.insert(min, il1);
                    }
                }
            }
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
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain.getOpcode() == Opcodes.INVOKEINTERFACE) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    if (TransformerManager.checkClassName(min.owner, "java/util/concurrent/ExecutorService") && TransformerManager.checkMethodName(min.owner, min.name, min.desc, "submit") && TransformerManager.checkMethodDesc(min.desc, "(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;")) {
                        mn.instructions.insertBefore(min, new VarInsnNode(Opcodes.ALOAD, 1));
                        mn.instructions.set(min, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadProfileTextures", "(Ljava/lang/Runnable;Lcom/mojang/authlib/GameProfile;)V", false));
                    }
                }
            }
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
            il.add(new VarInsnNode(Opcodes.ALOAD, 1));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadSkinFromCache", "(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;", false));
            il.add(new InsnNode(Opcodes.ARETURN));
            il.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            mn.instructions.insert(il);
            return mn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.resources.SkinManager$3",
        methodNameSrg = "run",
        methodNames = "run",
        desc = "()V"
    )
    public static class Run0Transformer implements TransformerManager.IMethodTransformer {
        @Override
        public MethodNode transform(ClassNode cn, MethodNode mn) {
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain.getOpcode() == Opcodes.INVOKEINTERFACE) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    if (TransformerManager.checkClassName(min.owner, "com/mojang/authlib/minecraft/MinecraftSessionService") && TransformerManager.checkMethodName(min.owner, min.name, min.desc, "getTextures") && TransformerManager.checkMethodDesc(min.desc, "(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;")) {
                        mn.instructions.set(min, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "getUserProfile", "(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;", false));
                        break;
                    }
                }
            }
            return mn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.resources.SkinManager$3$1",
        methodNameSrg = "run",
        methodNames = "run",
        desc = "()V"
    )
    public static class Run1Transformer implements TransformerManager.IMethodTransformer {
        @Override
        public MethodNode transform(ClassNode cn, MethodNode mn) {
            String val$skinAvailableCallback = null;
            String this$0 = null;

            String val$map = null;
            String this$1 = null;

            for (FieldNode fn : cn.fields) {
                if (val$map == null && TransformerManager.checkFieldDesc(fn.desc, "Ljava/util/Map;")) {
                    val$map = TransformerManager.mapFieldName(cn.name, fn.name, fn.desc);
                } else if (this$1 == null && TransformerManager.checkFieldDesc(fn.desc, "Lnet/minecraft/client/resources/SkinManager$3;")) {
                    this$1 = TransformerManager.mapFieldName(cn.name, fn.name, fn.desc);
                }
            }

            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain.getOpcode() == Opcodes.GETFIELD) {
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    if (TransformerManager.checkClassName(fin.owner, "net/minecraft/client/resources/SkinManager$3")) {
                        if (TransformerManager.checkFieldDesc(fin.desc, "Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;")) {
                            val$skinAvailableCallback = TransformerManager.mapFieldName(fin.owner, fin.name, fin.desc);
                        } else if (TransformerManager.checkFieldDesc(fin.desc, "Lnet/minecraft/client/resources/SkinManager;")) {
                            this$0 = TransformerManager.mapFieldName(fin.owner, fin.name, fin.desc);
                        }
                    }
                }
            }

            if (val$skinAvailableCallback != null && this$0 != null && val$map != null && this$1 != null) {
                AbstractInsnNode frame = null;
                for (AbstractInsnNode ain : mn.instructions.toArray()) {
                    if (ain instanceof FrameNode) {
                        frame = ain;
                    }
                    if (ain.getOpcode() == Opcodes.RETURN) {
                        InsnList il = new InsnList();
                        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        il.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager$3$1", this$1, "Lnet/minecraft/client/resources/SkinManager$3;"));
                        il.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager$3", this$0, "Lnet/minecraft/client/resources/SkinManager;"));
                        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        il.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager$3$1", val$map, "Ljava/util/Map;"));
                        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        il.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager$3$1", this$1, "Lnet/minecraft/client/resources/SkinManager$3;"));
                        il.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager$3", val$skinAvailableCallback, "Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;"));
                        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadElytraTexture", "(Lnet/minecraft/client/resources/SkinManager;Ljava/util/Map;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)V", false));
                        mn.instructions.insertBefore(ain, il);
                    }
                }
                mn.instructions.set(frame, new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            }

            return mn;
        }
    }
}
