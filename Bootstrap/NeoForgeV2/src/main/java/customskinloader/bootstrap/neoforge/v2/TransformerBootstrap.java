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

final class TransformerBootstrap implements ClassProcessor {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;
    private static final TransformerBootstrapSupport SUPPORT = new TransformerBootstrapSupport(TransformerBootstrap.class, "official");
    private static final ProcessorName PROCESSOR_NAME = new ProcessorName("customskinloader", "bootstrap");

    TransformerBootstrap() {
        SUPPORT.getTargetClassNames();
        LOGGER.info("Created CustomSkinLoader Bootstrap NeoForge class processor");
    }

    @Override
    public ProcessorName name() {
        return PROCESSOR_NAME;
    }

    @Override
    public Set<ProcessorName> runsAfter() {
        return Collections.singleton(ClassProcessorIds.SIMPLE_PROCESSORS_GROUP);
    }

    @Override
    public boolean handlesClass(SelectionContext context) {
        // The shared framework currently targets concrete classes only, so we skip synthetic empty definitions.
        return !context.empty() && SUPPORT.getTargetClassNames().contains(context.type().getInternalName());
    }

    @Override
    public ComputeFlags processClass(TransformationContext context) {
        ClassTransformationReport report = SUPPORT.transform(context.type().getInternalName(), context.node());
        if (!report.isModified()) {
            return ComputeFlags.NO_REWRITE;
        }

        for (String ruleName : report.getAppliedRuleNames()) {
            context.audit("customskinloader.bootstrap", ruleName);
        }
        LOGGER.info("Transformed NeoForge target " + context.type().getInternalName() + " with " + report.getAppliedRuleNames());
        return ComputeFlags.COMPUTE_FRAMES;
    }

    @Override
    public OrderingHint orderingHint() {
        return OrderingHint.LATE;
    }
}
