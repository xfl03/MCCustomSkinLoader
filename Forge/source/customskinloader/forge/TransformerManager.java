package customskinloader.forge;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import customskinloader.Logger;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class TransformerManager {
    public static Logger logger = new Logger(new File("./CustomSkinLoader/ForgePlugin.log"));

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TransformTarget {
        String className();

        String[] methodNames() default {};

        String desc() default "";
    }

    public interface IClassTransformer {
        void transform(ClassNode cn);
    }

    public interface IMethodTransformer {
        void transform(ClassNode cn, MethodNode mn);
    }

    public Map<String, IClassTransformer> classMap = new HashMap<>();
    public Map<String, Map<String, IMethodTransformer>> map = new HashMap<>();

    public TransformerManager(IMethodTransformer... methodTransformers) {
        this(new IClassTransformer[0], methodTransformers);
    }

    public TransformerManager(IClassTransformer[] classTransformers, IMethodTransformer[] methodTransformers) {
        for (IClassTransformer t : classTransformers) {
            TransformTarget tt = this.getTransformTarget(t.getClass());
            if (tt != null) {
                addClassTransformer(tt.className(), t);
            }
        }
        for (IMethodTransformer t : methodTransformers) {
            TransformTarget tt = this.getTransformTarget(t.getClass());
            if (tt != null) {
                addMethodTransformer(tt, tt.className(), t);
            }
        }
    }

    private TransformTarget getTransformTarget(Class<?> cl) {
        logger.info("[CSL DEBUG] REGISTERING TRANSFORMER %s", cl.getName());
        if (!cl.isAnnotationPresent(TransformTarget.class)) {
            logger.info("[CSL DEBUG] ERROR occurs while parsing Annotation.");
            return null;
        }
        return cl.getAnnotation(TransformTarget.class);
    }

    private void addClassTransformer(String className, IClassTransformer transformer) {
        if (!classMap.containsKey(className)) {
            classMap.put(className, transformer);
            logger.info("[CSL DEBUG] REGISTERING CLASS %s", className);
        }
    }

    private void addMethodTransformer(TransformTarget target, String className, IMethodTransformer transformer) {
        if (!map.containsKey(className))
            map.put(className, new HashMap<String, IMethodTransformer>());
        for (String methodName : target.methodNames()) {
            map.get(className).put(methodName + target.desc(), transformer);
            logger.info("[CSL DEBUG] REGISTERING METHOD %s(%s)", className, methodName + target.desc());
        }
    }

    public ClassNode transform(ClassNode classNode) {
        IClassTransformer transformer = classMap.get(FMLDeobfuscatingRemapper.INSTANCE.map(classNode.name).replace("/", "."));
        if (transformer != null) {
            try {
                transformer.transform(classNode);
                logger.info("[CSL DEBUG] Successfully transformed class %s", classNode.name);
            } catch (Exception e) {
                logger.warning("[CSL DEBUG] An error happened when transforming class %s.", classNode.name);
                logger.warning(e);
            }
        }
        return classNode;
    }

    public MethodNode transform(ClassNode classNode, MethodNode methodNode, String className, String methodName, String methodDesc) {
        Map<String, IMethodTransformer> transMap = map.get(className);
        String methodTarget = methodName + methodDesc;
        if (transMap != null && transMap.containsKey(methodTarget)) {
            try {
                transMap.get(methodTarget).transform(classNode, methodNode);
                logger.info("[CSL DEBUG] Successfully transformed method %s in class %s", methodName, className);
            } catch (Exception e) {
                logger.warning("[CSL DEBUG] An error happened when transforming method %s in class %s.", methodTarget, className);
                logger.warning(e);
            }
        }
        return methodNode;
    }
}
