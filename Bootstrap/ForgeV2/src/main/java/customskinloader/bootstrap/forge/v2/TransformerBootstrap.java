package customskinloader.bootstrap.forge.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.transformer.ClassTransformationReport;
import customskinloader.bootstrap.transformer.TransformerBootstrapSupport;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

final class TransformerBootstrap {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;
    private static final TransformerBootstrapSupport SUPPORT = new TransformerBootstrapSupport(TransformerBootstrap.class,
        Launcher.INSTANCE.environment().findLaunchHandler("forgeclient")
            .map(handle -> handle.getClass().getName().startsWith("net.neoforged.")).orElse(false) ? "official" : "srg");

    private TransformerBootstrap() {
    }

    public static List<ITransformer> buildTransformers() {
        List<ITransformer> transformers = new ArrayList<>();
        for (String targetClassName : SUPPORT.collectTargetClassNames()) {
            transformers.add(new ModLauncherClassTransformer(targetClassName));
            LOGGER.debug("Created ModLauncher transformer for " + targetClassName);
        }

        return Collections.unmodifiableList(transformers);
    }

    private static ClassNode transformClassNode(String internalClassName, ClassNode inputClassNode) {
        ClassTransformationReport report = SUPPORT.transformClassNode(internalClassName, inputClassNode);
        if (!report.isModified() || report.getTransformedClassNode() == null) {
            return inputClassNode;
        }

        LOGGER.info("Transformed ModLauncher target " + internalClassName + " with " + report.getAppliedTransformerNames());
        return report.getTransformedClassNode();
    }

    private static final class ModLauncherClassTransformer implements ITransformer<ClassNode> {
        private final String targetClassName;
        private final Set<ITransformer.Target> targets;

        private ModLauncherClassTransformer(String targetClassName) {
            this.targetClassName = targetClassName;

            Set<ITransformer.Target> transformerTargets = new LinkedHashSet<>();
            transformerTargets.add(ITransformer.Target.targetClass(targetClassName));
            this.targets = Collections.unmodifiableSet(transformerTargets);
        }

        @Override
        public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
            return transformClassNode(this.targetClassName, input);
        }

        @Override
        public TransformerVoteResult castVote(ITransformerVotingContext context) {
            return TransformerVoteResult.YES;
        }

        @Override
        public Set<ITransformer.Target> targets() {
            return this.targets;
        }

        // NeoForge
        public cpw.mods.modlauncher.api.TargetType getTargetType() {
            return  cpw.mods.modlauncher.api.TargetType.CLASS;
        }

        @Override
        public String[] labels() {
            return new String[]{"customskinloader:" + this.targetClassName.replace('/', '.')};
        }
    }
}
