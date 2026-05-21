package customskinloader.bootstrap.transformer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.mapping.Loader;
import customskinloader.bootstrap.mapping.Mappings;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public final class TransformerBootstrapSupport {
    private static final Field[] CLASS_NODE_FIELDS = ClassNode.class.getFields();
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    private final Object lock = new Object();
    private final Class<?> serviceLoaderAnchor;
    private final String mappingType;
    private final ClassTransformerRegistry registry = new ClassTransformerRegistry();
    private final RuntimeClassTransformerEngine transformerEngine = new RuntimeClassTransformerEngine(this.registry);

    private volatile boolean transformersLoaded;
    private volatile Mappings mappings;

    public TransformerBootstrapSupport(Class<?> serviceLoaderAnchor, String mappingType) {
        if (serviceLoaderAnchor == null) {
            throw new IllegalArgumentException("Service loader anchor must not be null");
        }

        this.serviceLoaderAnchor = serviceLoaderAnchor;
        this.mappingType = mappingType;
    }

    public void ensureTransformersLoaded() {
        if (this.transformersLoaded) {
            return;
        }

        synchronized (this.lock) {
            if (this.transformersLoaded) {
                return;
            }

            // Discover platform-contributed targeted transformers lazily through the shared service contract.
            ServiceLoader<TargetedClassTransformer> serviceLoader = ServiceLoader.load(TargetedClassTransformer.class, this.serviceLoaderAnchor.getClassLoader());
            int transformerCount = 0;
            for (TargetedClassTransformer transformer : serviceLoader) {
                this.registry.register(transformer);
                transformerCount++;
                LOGGER.debug("Registered transformer " + transformer.getName()
                    + " with priority " + transformer.getPriority()
                    + " and targets " + transformer.getTargetClassNames());
            }

            this.transformersLoaded = true;
            LOGGER.info("Loaded " + transformerCount + " CustomSkinLoader bootstrap transformer(s) for " + this.mappingType + " mappings");
        }
    }

    public Set<String> collectTargetClassNames() {
        this.ensureTransformersLoaded();
        Mappings loadedMappings = this.getMappings();

        Set<String> targetClassNames = new LinkedHashSet<>();
        for (TargetedClassTransformer transformer : this.registry.getTransformers()) {
            for (String targetClassName : transformer.getTargetClassNames()) {
                targetClassNames.add(loadedMappings.remapClassName(targetClassName));
            }
        }

        LOGGER.info("Collected " + targetClassNames.size() + " CustomSkinLoader bootstrap target class(es) for " + this.mappingType + " mappings");
        return Collections.unmodifiableSet(targetClassNames);
    }

    public boolean hasApplicableTransformers(String internalClassName) {
        this.ensureTransformersLoaded();
        return !this.registry.getApplicableTransformers(internalClassName, this.getMappings()).isEmpty();
    }

    public ClassTransformationReport transformClassNode(String internalClassName, ClassNode inputClassNode) {
        this.ensureTransformersLoaded();
        ClassTransformationContext context = ClassTransformationContext.create(internalClassName, inputClassNode, this.getMappings());
        return this.transformerEngine.transform(context);
    }

    public Mappings getMappings() {
        Mappings loadedMappings = this.mappings;
        if (loadedMappings != null) {
            return loadedMappings;
        }

        synchronized (this.lock) {
            loadedMappings = this.mappings;
            if (loadedMappings == null) {
                LOGGER.info("Loading CustomSkinLoader bootstrap mappings for " + this.mappingType);
                loadedMappings = Loader.load(this.serviceLoaderAnchor.getClassLoader(), this.mappingType);
                this.mappings = loadedMappings;
                LOGGER.info("Loaded " + loadedMappings.getClassMappings().size() + " CustomSkinLoader bootstrap class mapping(s) for " + this.mappingType);
            }
        }

        return loadedMappings;
    }

    public static byte[] toByteArray(ClassNode classNode) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    public static ClassNode toClassNode(byte[] classBytecode) {
        ClassReader classReader = new ClassReader(classBytecode);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    public static void copyClassNode(ClassNode source, ClassNode target) {
        try {
            for (Field field : CLASS_NODE_FIELDS) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.set(target, field.get(source));
                }
            }
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to copy transformed ClassNode state", exception);
        }
    }
}
