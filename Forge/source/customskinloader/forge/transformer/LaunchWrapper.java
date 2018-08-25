package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class LaunchWrapper implements IClassTransformer {
    private TransformerManager transformerManager = new TransformerManager();

    //LaunchWrapper
    //From: https://github.com/RecursiveG/UniSkinMod/blob/1.9.4/src/main/java/org/devinprogress/uniskinmod/coremod/BaseAsmTransformer.java#L83-L114
    public byte[] transform(String obfClassName, String className, byte[] bytes) {
        if (!transformerManager.map.containsKey(className)) return bytes;//Check if should be transformed

        //Parse bytes to ClassNode
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        //Transform ClassNode
        transformerManager.transform(cn, className, obfClassName);

        //Parse Class Node to bytes
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
