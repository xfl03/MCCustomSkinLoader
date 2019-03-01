package customskinloader.forge.loader;

import customskinloader.forge.TransformerManager;
import customskinloader.forge.transformer.PlayerTabTransformer;
import customskinloader.forge.transformer.SkinManagerTransformer;
import customskinloader.forge.transformer.SpectatorMenuTransformer;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

//LaunchWrapper for 1.13-
public class LaunchWrapper implements IClassTransformer {

    private static final TransformerManager.IMethodTransformer[] TRANFORMERS = {
            new SkinManagerTransformer.InitTransformer(),
            new SkinManagerTransformer.LoadSkinTransformer(),
            new SkinManagerTransformer.LoadProfileTexturesTransformer(),
            new SkinManagerTransformer.LoadSkinFromCacheTransformer(),
            new PlayerTabTransformer.ScoreObjectiveTransformer(),
            new SpectatorMenuTransformer.PlayerMenuObjectTransformer()
    };
    private TransformerManager transformerManager = new TransformerManager(TRANFORMERS);

    //From: https://github.com/RecursiveG/UniSkinMod/blob/1.9.4/src/main/java/org/devinprogress/uniskinmod/coremod/BaseAsmTransformer.java#L83-L114
    public byte[] transform(String obfClassName, String className, byte[] bytes) {
        if (!transformerManager.map.containsKey(className)) return bytes;//Check if should be transformed

        //Parse bytes to ClassNode
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        //Transform ClassNode
        for (MethodNode mn : cn.methods) {
            String methodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(obfClassName, mn.name, mn.desc);
            String methodDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(mn.desc);
            transformerManager.transform(cn, mn, className, methodName, methodDesc);
        }

        //Parse Class Node to bytes
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
