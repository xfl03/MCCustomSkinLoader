package customskinloader.forge;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import customskinloader.Logger;
import customskinloader.forge.transformer.*;
import customskinloader.forge.transformer.PlayerTabTransformer.ScoreObjectiveTransformer;
import customskinloader.forge.transformer.SkinManagerTransformer.*;
import customskinloader.forge.transformer.SpectatorMenuTransformer.PlayerMenuObjectTransformer;

public class TransformerManager {
    public static Logger logger = new Logger(new File("./CustomSkinLoader/ForgePlugin.log"));

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TransformTarget {
        String className();

        String[] methodNames();

        String desc();
    }

    public interface IMethodTransformer {
        void transform(ClassNode cn, MethodNode mn);
    }

    private static final IMethodTransformer[] TRANFORMERS = {
            new InitTransformer(),
            new LoadSkinTransformer(),
            new LoadProfileTexturesTransformer(),
            new LoadSkinFromCacheTransformer(),
            new ScoreObjectiveTransformer(),
            new PlayerMenuObjectTransformer(),
            new FakeSkinManagerTransformer.InitTransformer()
    };
    public Map<String, Map<String, IMethodTransformer>> map;

    public TransformerManager() {
        map = new HashMap<String, Map<String, IMethodTransformer>>();
        for (IMethodTransformer t : TRANFORMERS) {
            logger.info("[CSL DEBUG] REGISTERING TRANSFORMER %s", t.getClass().getName());
            if (!t.getClass().isAnnotationPresent(TransformTarget.class)) {
                logger.info("[CSL DEBUG] ERROR occurs while parsing Annotation.");
                continue;
            }
            addMethodTransformer(t.getClass().getAnnotation(TransformTarget.class), t);
        }
    }

    private void addMethodTransformer(TransformTarget target, IMethodTransformer transformer) {
        if (!map.containsKey(target.className()))
            map.put(target.className(), new HashMap<String, IMethodTransformer>());
        for (String methodName : target.methodNames()) {
            map.get(target.className()).put(methodName + target.desc(), transformer);
            logger.info("[CSL DEBUG] REGISTERING METHOD %s(%s)", target.className(), methodName + target.desc());
        }
    }

    public ClassNode transform(ClassNode classNode) {
        String obfClassName = classNode.name;
        String className = FMLDeobfuscatingRemapper.INSTANCE.map(obfClassName);
        return transform(classNode, className, obfClassName);
    }

    public ClassNode transform(ClassNode classNode, String className, String obfClassName) {
        logger.info("[CSL DEBUG] CLASS %s will be transformed", className);
        Map<String, IMethodTransformer> transMap = map.get(className);

        // NOTE: `map` = convert obfuscated name to srgName;
        List<MethodNode> ml = new ArrayList<MethodNode>(classNode.methods);
        for (MethodNode mn : ml) {
            String methodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(obfClassName, mn.name, mn.desc);
            String methodDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(mn.desc);
            String methodTarget = methodName + methodDesc;
            if (transMap.containsKey(methodTarget)) {
                try {
                    transMap.get(methodTarget).transform(classNode, mn);
                    logger.info("[CSL DEBUG] Successfully transformed method %s in class %s", methodName, className);
                } catch (Exception e) {
                    logger.warning("[CSL DEBUG] An error happened when transforming method %s in class %s.", methodTarget, className);
                    logger.warning(e);
                }
            }
        }
        return classNode;
    }
}
