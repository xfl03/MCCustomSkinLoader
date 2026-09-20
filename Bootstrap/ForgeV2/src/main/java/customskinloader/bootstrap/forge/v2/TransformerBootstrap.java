package customskinloader.bootstrap.forge.v2;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import cpw.mods.modlauncher.ArgumentHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.ModLoaderInfo;
import customskinloader.bootstrap.transformer.ClassTransformationReport;
import customskinloader.bootstrap.transformer.TransformerBootstrapSupport;
import customskinloader.bootstrap.util.ReflectionUtils;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

final class TransformerBootstrap implements ITransformer<ClassNode> {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;
    private static final TransformerBootstrapSupport SUPPORT = new TransformerBootstrapSupport(TransformerBootstrap.class,
        // official: neoforge 1.20.2-20.2.3 ~ 1.21.3-21.3.97
        // srg: forge *, neoforge 1.21.4-21.4.0 ~ 1.21.9-21.9.13
        Launcher.INSTANCE.environment().findLaunchHandler("forgeclient")
            .map(handle -> handle.getClass().getName().startsWith("net.neoforged.")).orElse(false) ? "official" : "srg");

    public static void publishModLoaderInfo() {
        try {
            Object argumentHandler = ReflectionUtils.findField(Launcher.class, "argumentHandler").get(Launcher.INSTANCE);
            String[] args = (String[]) ReflectionUtils.findField(ArgumentHandler.class, "args").get(argumentHandler);
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (Objects.equals(arg, "--fml.forgeVersion")) {
                    // forge 1.13.2-25.0.216 ~ 1.20.2-48.1.0
                    ModLoaderInfo.publish("Forge", args[i + 1]);
                    return;
                } else if (Objects.equals(arg, "--fml.neoForgeVersion")) {
                    // neoforge 1.20.2-20.2.3 ~ 1.21.9-21.9.13
                    ModLoaderInfo.publish("NeoForge", args[i + 1]);
                    return;
                }
            }

            // forge 1.20.3-49.0.1+
            ModLoaderInfo.publish("Forge", FMLLoader.class.getPackage().getImplementationVersion());
        } catch (Exception e) {
            LOGGER.warn("Could not determine mod loader", e);
            ModLoaderInfo.publish("Forge", "unknown");
        }
    }

    private final Set<ITransformer.Target> targets;

    TransformerBootstrap() {
        Set<ITransformer.Target> transformerTargets = new LinkedHashSet<>();
        for (String targetClassName : SUPPORT.getTargetClassNames()) {
            transformerTargets.add(ITransformer.Target.targetClass(targetClassName));
        }
        this.targets = Collections.unmodifiableSet(transformerTargets);
        LOGGER.info("Created one ModLauncher transformer for " + this.targets.size() + " target class(es)");
    }

    @Override
    public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        String internalClassName = context.getClassName().replace('.', '/');
        ClassTransformationReport report = SUPPORT.transform(internalClassName, input);
        if (report.isModified()) {
            LOGGER.info("Transformed ModLauncher target " + internalClassName + " with " + report.getAppliedRuleNames());
        }
        return report.getTransformedClassNode();
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
        return cpw.mods.modlauncher.api.TargetType.CLASS;
    }

    @Override
    public String[] labels() {
        return new String[] {"customskinloader:bootstrap"};
    }
}
