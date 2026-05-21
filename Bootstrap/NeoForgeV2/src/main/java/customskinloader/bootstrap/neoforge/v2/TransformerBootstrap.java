package customskinloader.bootstrap.neoforge.v2;

import java.util.Collections;
import java.util.Set;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.transformer.ClassTransformationReport;
import customskinloader.bootstrap.transformer.TransformerBootstrapSupport;
import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ClassProcessorIds;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

final class TransformerBootstrap {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;
    private static final TransformerBootstrapSupport SUPPORT = new TransformerBootstrapSupport(TransformerBootstrap.class, "official");
    private static final ProcessorName PROCESSOR_NAME = new ProcessorName("customskinloader", "bootstrap");

    private TransformerBootstrap() {
    }

    public static ClassProcessor createProcessor() {
        SUPPORT.ensureTransformersLoaded();
        LOGGER.info("Created CustomSkinLoader Bootstrap NeoForge class processor");
        return new BootstrapClassProcessor();
    }

    private static ClassTransformationReport transformClassNode(String internalClassName, ClassNode inputClassNode) {
        return SUPPORT.transformClassNode(internalClassName, inputClassNode);
    }

    private static final class BootstrapClassProcessor implements ClassProcessor {
        @Override
        public ProcessorName name() {
            return PROCESSOR_NAME;
        }

        @Override
        public Set<ProcessorName> runsAfter() {
            // Run after the default simple-processor group so the shared framework sees post-Mixin bytecode.
            return Collections.singleton(ClassProcessorIds.SIMPLE_PROCESSORS_GROUP);
        }

        @Override
        public boolean handlesClass(SelectionContext context) {
            // The shared framework currently targets concrete classes only, so we skip synthetic empty definitions.
            return !context.empty() && SUPPORT.hasApplicableTransformers(context.type().getInternalName());
        }

        @Override
        public ComputeFlags processClass(TransformationContext context) {
            ClassTransformationReport report = transformClassNode(context.type().getInternalName(), context.node());
            if (!report.isModified()) {
                return ComputeFlags.NO_REWRITE;
            }

            TransformerBootstrapSupport.copyClassNode(report.getTransformedClassNode(), context.node());
            for (String transformerName : report.getAppliedTransformerNames()) {
                context.audit("customskinloader.bootstrap", transformerName);
            }

            LOGGER.info("Transformed NeoForge target " + context.type().getInternalName() + " with " + report.getAppliedTransformerNames());
            return ComputeFlags.COMPUTE_FRAMES;
        }

        @Override
        public OrderingHint orderingHint() {
            return OrderingHint.LATE;
        }
    }
}
