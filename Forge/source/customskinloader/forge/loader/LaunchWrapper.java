package customskinloader.forge.loader;

import customskinloader.forge.TransformerManager;
import customskinloader.forge.transformer.FakeInterfacesTransformer;
import customskinloader.forge.transformer.PlayerTabTransformer;
import customskinloader.forge.transformer.SkinManagerTransformer;
import customskinloader.forge.transformer.SpectatorMenuTransformer;
import customskinloader.forge.transformer.TileEntitySkullTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

//LaunchWrapper for 1.13-
public class LaunchWrapper implements IClassTransformer {
    private static final TransformerManager.IClassTransformer[] CLASS_TRANSFORMERS = {
        new FakeInterfacesTransformer.MinecraftTransformer(),
        new FakeInterfacesTransformer.AbstractTextureTransfomer(),
        new FakeInterfacesTransformer.TextureTransformer(),
        new FakeInterfacesTransformer.TextureManagerTransformer()
    };
    private static final TransformerManager.IMethodTransformer[] TRANFORMERS = {
        new SkinManagerTransformer.InitTransformer(),
        new SkinManagerTransformer.LoadSkinTransformer(),
        new SkinManagerTransformer.LoadProfileTexturesTransformer(),
        new SkinManagerTransformer.LoadSkinFromCacheTransformer(),
        new PlayerTabTransformer.ScoreObjectiveTransformer(),
        new SpectatorMenuTransformer.PlayerMenuObjectTransformer(),
        new TileEntitySkullTransformer.UpdateGameProfileTransformer()
    };
    private TransformerManager transformerManager = new TransformerManager(CLASS_TRANSFORMERS, TRANFORMERS);

    //From: https://github.com/RecursiveG/UniSkinMod/blob/1.9.4/src/main/java/org/devinprogress/uniskinmod/coremod/BaseAsmTransformer.java#L83-L114
    public byte[] transform(String obfClassName, String className, byte[] bytes) {
        if (!transformerManager.classMap.containsKey(className) && !transformerManager.map.containsKey(className)) return bytes;//Check if should be transformed

        //Parse bytes to ClassNode
        ClassNode cn = new ClassNode();
        if (bytes != null && bytes.length > 0) {
            ClassReader cr = new ClassReader(bytes);
            cr.accept(cn, 0);
        } else {
            cn.name = className.replace(".", "/");
            cn.version = Opcodes.V1_8;
            cn.superName = "java/lang/Object";
        }

        //Transform ClassNode
        transformerManager.transform(cn);
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
