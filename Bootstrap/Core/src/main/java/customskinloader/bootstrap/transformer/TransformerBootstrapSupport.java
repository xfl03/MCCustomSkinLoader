package customskinloader.bootstrap.transformer;

import java.util.ServiceLoader;
import java.util.Set;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.mapping.Loader;
import customskinloader.bootstrap.mapping.Mappings;
import customskinloader.bootstrap.mapping.VersionMetadataReader;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public final class TransformerBootstrapSupport {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    private final Object lock = new Object();
    private final Class<?> serviceLoaderAnchor;
    private final String mappingType;
    private volatile TransformationPlan transformationPlan;

    public TransformerBootstrapSupport(Class<?> serviceLoaderAnchor, String mappingType) {
        if (serviceLoaderAnchor == null) {
            throw new IllegalArgumentException("Service loader anchor must not be null");
        }
        this.serviceLoaderAnchor = serviceLoaderAnchor;
        this.mappingType = mappingType;
    }

    public Set<String> getTargetClassNames() {
        return this.getTransformationPlan().getTargetClassNames();
    }

    public ClassTransformationReport transform(String internalClassName, byte[] classBytecode) {
        return this.getTransformationPlan().transform(internalClassName, classBytecode);
    }

    public ClassTransformationReport transform(String internalClassName, ClassNode inputClassNode) {
        return this.getTransformationPlan().transform(internalClassName, inputClassNode);
    }

    private TransformationPlan getTransformationPlan() {
        TransformationPlan loadedPlan = this.transformationPlan;
        if (loadedPlan != null) {
            return loadedPlan;
        }

        synchronized (this.lock) {
            loadedPlan = this.transformationPlan;
            if (loadedPlan == null) {
                LOGGER.info("Compiling CustomSkinLoader transformation plan for " + this.mappingType + " mappings");
                Mappings mappings = Loader.load(this.serviceLoaderAnchor.getClassLoader(), this.mappingType);
                // Discover platform-contributed targeted transformers lazily through the shared service contract.
                ServiceLoader<TransformationRuleProvider> providers = ServiceLoader.load(
                    TransformationRuleProvider.class, this.serviceLoaderAnchor.getClassLoader());
                loadedPlan = new TransformationPlan(
                    providers,
                    new RuntimeVersion(VersionMetadataReader.PROTOCOL_VERSION, VersionMetadataReader.WORLD_VERSION),
                    mappings,
                    new TransformationObserver() {
                        @Override
                        public void ruleApplied(String ruleId, String internalClassName) {
                            LOGGER.debug("Applied transformation rule " + ruleId + " to " + internalClassName);
                        }

                        @Override
                        public void optionalRuleSkipped(String message) {
                            LOGGER.warn(message);
                        }

                        @Override
                        public void classTransformed(String internalClassName, Iterable<String> ruleIds) {
                            LOGGER.info("Transformed " + internalClassName + " with " + ruleIds);
                        }
                    }
                );
                this.transformationPlan = loadedPlan;
                LOGGER.info("Compiled " + loadedPlan.getActiveRuleCount() + " of " + loadedPlan.getDeclaredRuleCount()
                    + " transformation rule(s) for " + loadedPlan.getTargetClassNames().size() + " target class(es)");
            }
        }
        return loadedPlan;
    }

    public static byte[] toByteArray(ClassNode classNode) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

}
