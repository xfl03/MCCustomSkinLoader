package customskinloader.forge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import customskinloader.forge.platform.IFMLPlatform;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class TransformerManager {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TransformTarget {
        String className();

        String methodNameSrg() default "";

        String[] methodNames() default {};

        String desc() default "";
    }

    public interface IClassTransformer {
        ClassNode transform(ClassNode cn);
    }

    public interface IMethodTransformer {
        MethodNode transform(ClassNode cn, MethodNode mn);
    }

    public static boolean isDevelopmentEnvironment = false;
    /** If true, match {@link TransformTarget#methodNames} if {@link TransformTarget#methodNameSrg} fails to match. */
    public static boolean useDeobfName = false;
    private final static Remapper remapper = IFMLPlatform.FMLPlatformInitializer.getPlatform().getRemapper();

    public static String mapClassName(String name) {
        return remapper.mapType(name);
    }

    public static String mapFieldName(String owner, String name, String desc) {
        return remapper.mapFieldName(owner, name, desc);
    }

    public static String mapMethodName(String owner, String name, String desc) {
        return remapper.mapMethodName(owner, name, desc);
    }

    public static String mapMethodDesc(String desc) {
        return remapper.mapMethodDesc(desc);
    }

    public static boolean checkClassName(String name, String deobfName) {
        if (isDevelopmentEnvironment) {
            return name.equals(deobfName);
        } else {
            return mapClassName(name).equals(deobfName);
        }
    }

    public static boolean checkMethodName(String owner, String name, String desc, String srgName) {
        if (isDevelopmentEnvironment) {
            return mapMethodName(owner, srgName, desc).equals(name);
        } else {
            return mapMethodName(owner, name, desc).equals(srgName);
        }
    }

    public static boolean checkMethodDesc(String desc, String deobfDesc)  {
        if (isDevelopmentEnvironment) {
            return desc.equals(deobfDesc);
        } else {
            return mapMethodDesc(desc).equals(deobfDesc);
        }
    }

    public Map<String, IClassTransformer> classMap = new HashMap<>();
    public Map<String, Map<Supplier<String>, IMethodTransformer>> srgMap = new HashMap<>();
    public Map<String, Map<String, IMethodTransformer>> map = new HashMap<>();

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
        ForgeTweaker.logger.info("[CSL DEBUG] REGISTERING TRANSFORMER %s", cl.getName());
        if (!cl.isAnnotationPresent(TransformTarget.class)) {
            ForgeTweaker.logger.info("[CSL DEBUG] ERROR occurs while parsing Annotation");
            return null;
        }
        return cl.getAnnotation(TransformTarget.class);
    }

    private void addClassTransformer(String className, IClassTransformer transformer) {
        if (!classMap.containsKey(className)) {
            classMap.put(className, transformer);
            ForgeTweaker.logger.info("[CSL DEBUG] REGISTERING CLASS %s", className);
        }
    }

    private void addMethodTransformer(TransformTarget target, String className, IMethodTransformer transformer) {
        if (!srgMap.containsKey(className)) {
            srgMap.put(className, new HashMap<>());
        }
        if (!target.methodNameSrg().equals("")) {
            // FMLDeobfRemapper has not been setup when this line being reached.
            Supplier<String> mappedMethod = () -> (isDevelopmentEnvironment ? mapMethodName(className.replace(".", "/"), target.methodNameSrg(), target.desc()) : target.methodNameSrg()) + target.desc();
            srgMap.get(className).put(mappedMethod, transformer);
            ForgeTweaker.logger.info("[CSL DEBUG] REGISTERING SRG METHOD %s::%s", className, target.methodNameSrg() + target.desc());
        }
        if (!map.containsKey(className))
            map.put(className, new HashMap<>());
        for (String methodName : target.methodNames()) {
            map.get(className).put(methodName + target.desc(), transformer);
            ForgeTweaker.logger.info("[CSL DEBUG] REGISTERING METHOD %s::%s", className, methodName + target.desc());
        }
    }

    public ClassNode transform(ClassNode classNode, String className) {
        IClassTransformer transformer = classMap.get(className);
        if (transformer != null) {
            try {
                classNode = transformer.transform(classNode);
                ForgeTweaker.logger.info("[CSL DEBUG] Successfully transformed class %s", className);
            } catch (Exception e) {
                ForgeTweaker.logger.warning("[CSL DEBUG] An error happened when transforming class %s", className);
                ForgeTweaker.logger.warning(e);
            }
        }
        return classNode;
    }

    public MethodNode transform(ClassNode classNode, MethodNode methodNode, String className, String methodName, String methodDesc) {
        String methodTarget = methodName + methodDesc;
        Map<Supplier<String>, IMethodTransformer> transSrgMap = srgMap.get(className);
        IMethodTransformer transformer = transSrgMap == null ? null : transSrgMap.get(transSrgMap.keySet().stream().filter(s -> s.get().equals(methodTarget)).findFirst().orElse(null));

        Map<String, IMethodTransformer> transMap = map.get(className);
        if (useDeobfName && transMap != null && transformer == null) {
            transformer = transMap.get(methodTarget);
        }
        if (transformer != null) {
            try {
                methodNode = transformer.transform(classNode, methodNode);
                ForgeTweaker.logger.info("[CSL DEBUG] Successfully transformed method %s in class %s", methodName, className);
            } catch (Exception e) {
                ForgeTweaker.logger.warning("[CSL DEBUG] An error happened when transforming method %s in class %s", methodTarget, className);
                ForgeTweaker.logger.warning(e);
            }
        }
        return methodNode;
    }
}
