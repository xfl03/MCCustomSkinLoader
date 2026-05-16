package customskinloader.bootstrap.transformer;

import java.util.ArrayList;
import java.util.List;

import customskinloader.bootstrap.BootstrapLogger;
import org.apache.logging.log4j.Logger;

public final class RuntimeClassTransformerEngine {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    private final ClassTransformerRegistry registry;

    public RuntimeClassTransformerEngine(ClassTransformerRegistry registry) {
        this.registry = registry;
    }

    public ClassTransformationReport transform(ClassTransformationContext context) {
        List<String> appliedTransformers = new ArrayList<>();

        for (TargetedClassTransformer transformer : this.registry.getApplicableTransformers(context.getInternalClassName(), context.getMappings())) {
            if (!this.applyTransformer(transformer, context)) {
                LOGGER.debug("Skipped transformer " + transformer.getName() + " for " + context.getInternalClassName());
                continue;
            }
            appliedTransformers.add(transformer.getName());
            LOGGER.debug("Applied transformer " + transformer.getName() + " to " + context.getInternalClassName());
        }

        if (!appliedTransformers.isEmpty()) {
            LOGGER.info("Transformed " + context.getInternalClassName() + " with " + appliedTransformers);
        }

        return new ClassTransformationReport(context, appliedTransformers);
    }

    private boolean applyTransformer(TargetedClassTransformer transformer, ClassTransformationContext context) {
        try {
            return transformer.transform(context);
        } catch (Exception exception) {
            String message = "Failed to transform " + context.getInternalClassName() + " with " + transformer.getName();
            LOGGER.error(message, exception);
            throw new IllegalStateException(message, exception);
        }
    }
}
