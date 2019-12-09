package customskinloader.forge;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import customskinloader.Logger;

import javax.annotation.Nullable;

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

    public Map<String, Map<String, IMethodTransformer>> map;

    public TransformerManager(IMethodTransformer... transformers) {
        map = new HashMap<String, Map<String, IMethodTransformer>>();
        for (IMethodTransformer t : transformers) {
            logger.info("[CSL DEBUG] REGISTERING TRANSFORMER %s", t.getClass().getName());
            if (!t.getClass().isAnnotationPresent(TransformTarget.class)) {
                logger.info("[CSL DEBUG] ERROR occurs while parsing Annotation.");
                continue;
            }

            TransformTarget tt = t.getClass().getAnnotation(TransformTarget.class);
            addMethodTransformer(tt, tt.className(), t);
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

    public MethodNode transform(ClassNode classNode, MethodNode methodNode, String className, String methodName, String methodDesc) {
        Map<String, IMethodTransformer> transMap = map.get(className);
        String methodTarget = methodName + methodDesc;
        if (transMap.containsKey(methodTarget)) {
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
