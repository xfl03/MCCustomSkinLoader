package customskinloader.forge.loader;

import java.util.ArrayList;

import customskinloader.forge.TransformerManager;
import customskinloader.forge.transformer.FakeInterfacesTransformer;
import customskinloader.forge.transformer.PlayerTabTransformer;
import customskinloader.forge.transformer.SkinManagerTransformer;
import customskinloader.forge.transformer.SpectatorMenuTransformer;
import customskinloader.forge.transformer.TileEntitySkullTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

//LaunchWrapper for 1.13-
public class LaunchWrapper implements IClassTransformer {
    private static final TransformerManager.IClassTransformer[] CLASS_TRANSFORMERS = {
        new FakeInterfacesTransformer.MinecraftTransformer(),
        new FakeInterfacesTransformer.ThreadDownloadImageDataTransformer(),
        new FakeInterfacesTransformer.AbstractTextureTransformer(),
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
        cn = transformerManager.transform(cn, className);
        ArrayList<MethodNode> methods = new ArrayList<>();
        for (MethodNode mn : cn.methods) {
            String mappedMethodName = TransformerManager.isDevelopmentEnvironment ? mn.name : TransformerManager.mapMethodName(cn.name, mn.name, mn.desc);
            String mappedMethodDesc = TransformerManager.isDevelopmentEnvironment ? mn.desc : TransformerManager.mapMethodDesc(mn.desc);
            methods.add(transformerManager.transform(cn, mn, className, mappedMethodName, mappedMethodDesc));
        }
        cn.methods.clear();
        cn.methods.addAll(methods);

        //Parse Class Node to bytes
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
