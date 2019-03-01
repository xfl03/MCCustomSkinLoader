package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import customskinloader.forge.TransformerManager.IMethodTransformer;
import customskinloader.forge.TransformerManager.TransformTarget;

import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * Transformer of FakeSkinManager for 1.13+
 * @deprecated Use JavaScript Instead
 */
@Deprecated
public class FakeSkinManagerTransformer {
    private final static String TARGET_CLASS = "net/minecraft/client/renderer/ThreadDownloadImageData";
    private final static String NEW_TARGET_CLASS = "net/minecraft/client/renderer/texture/ThreadDownloadImageData";
    private final static String CALLBACK_CLASS = "net/minecraft/client/resources/SkinManager$SkinAvailableCallback";


    @TransformTarget(className = "customskinloader.fake.FakeSkinManager",
            methodNames = {"<init>"},
            desc = "(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V")
    public static class InitTransformer implements IMethodTransformer {
        @Override
        public void transform(ClassNode cn, MethodNode mn) {
            TransformerManager.logger.info("1.13 detected, FakeSkinManager will be transformed.");
            //ClassNameRemapper.remapClassName(cn, TARGET_CLASS, NEW_TARGET_CLASS);
            //MethodNameRemapper.remapMethodName(cn, CALLBACK_CLASS, "func_180521_a", "onSkinTextureAvailable");

            InsnList il = mn.instructions;
            ListIterator<AbstractInsnNode> li = il.iterator();
            while (li.hasNext()) {
                AbstractInsnNode ain = li.next();
                if (ain instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) ain;

                    if (min.owner.equals(TARGET_CLASS)) {
                        min.owner = NEW_TARGET_CLASS;
                        //il.set(min, new MethodInsnNode(min.getOpcode(), NEW_TARGET_CLASS, min.name, min.desc, false));
                    }
                    if (min.owner.equals(CALLBACK_CLASS) && min.name.equals("func_180521_a")) {
                        min.name = "onSkinTextureAvailable";
                        //il.set(min, new MethodInsnNode(min.getOpcode(), min.owner, "onSkinTextureAvailable", min.desc, false));
                    }
                } else if (ain instanceof TypeInsnNode) {
                    TypeInsnNode tin = (TypeInsnNode) ain;

                    if (tin.desc.equals(TARGET_CLASS)) {
                        tin.desc = NEW_TARGET_CLASS;
                    }
                }
            }
        }
    }
}
