package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import customskinloader.forge.remapper.ClassNameRemapper;
import customskinloader.forge.TransformerManager.IMethodTransformer;
import customskinloader.forge.TransformerManager.TransformTarget;
import customskinloader.forge.remapper.MethodNameRemapper;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;

/**
 * Transformer of FakeSkinManager for 1.13+
 */
public class FakeSkinManagerTransformer {
    private static boolean needTransform = false;
    private static boolean needInit = true;
    private final static String TARGET_CLASS = "net/minecraft/client/renderer/ThreadDownloadImageData";
    private final static String NEW_TARGET_CLASS = "net/minecraft/client/renderer/texture/ThreadDownloadImageData";
    private final static String CALLBACK_CLASS = "net/minecraft/client/resources/SkinManager$SkinAvailableCallback";

    /* No Need for Detecting 1.13
    private static void init() {
        if (!needInit) return;
        needTransform = TARGET_CLASS == FMLDeobfuscatingRemapper.INSTANCE.unmap(TARGET_CLASS);
        needInit = false;
    }
    */

    @TransformTarget(className = "customskinloader.fake.FakeSkinManager",
            methodNames = {"<init>"},
            desc = "(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V")
    public static class InitTransformer implements IMethodTransformer {
        @Override
        public void transform(ClassNode cn, MethodNode mn) {
            /* No Need for Detecting 1.13
            init();
            if (!needTransform) return;
            */
            TransformerManager.logger.info("1.13 detected, FakeSkinManager will be transformed.");
            ClassNameRemapper.remapClassName(cn, TARGET_CLASS, NEW_TARGET_CLASS);
            MethodNameRemapper.remapMethodName(cn, CALLBACK_CLASS, "func_180521_a", "onSkinTextureAvailable");
        }
    }
}
