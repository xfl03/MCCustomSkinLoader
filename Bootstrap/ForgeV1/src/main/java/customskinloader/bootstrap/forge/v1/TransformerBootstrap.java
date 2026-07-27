package customskinloader.bootstrap.forge.v1;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.ModLoaderInfo;
import customskinloader.bootstrap.installer.CommonJarInstaller;
import customskinloader.bootstrap.transformer.ClassTransformationReport;
import customskinloader.bootstrap.transformer.TransformerBootstrapSupport;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

final class TransformerBootstrap {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;
    private static final TransformerBootstrapSupport SUPPORT = new TransformerBootstrapSupport(TransformerBootstrap.class, "srg");

    private TransformerBootstrap() {
    }

    public static void injectData(Map<String, Object> data) {
        try {
            Path runtimeDirectory = resolveRuntimeDirectory(data);
            LOGGER.info("Initializing CustomSkinLoader Bootstrap for Forge v1 in " + BootstrapLogger.formatPath(runtimeDirectory));
            ModLoaderInfo.publish("Forge", ForgeVersion.getVersion());
            Path commonJar = CommonJarInstaller.releaseCommonJar(runtimeDirectory, "srg");
            resolveLaunchClassLoader(data).addURL(commonJar.toUri().toURL());
            LOGGER.info("Added CustomSkinLoader Common jar to LaunchClassLoader: " + BootstrapLogger.formatPath(commonJar));
        } catch (Exception exception) {
            LOGGER.error("Failed to prepare CustomSkinLoader runtime jars", exception);
            throw new IllegalStateException("Failed to prepare CustomSkinLoader runtime jars", exception);
        }
    }

    public static byte[] transform(String className, byte[] classBytecode) {
        SUPPORT.ensureTransformersLoaded();

        if (!SUPPORT.hasApplicableTransformers(className)) {
            return classBytecode;
        }

        ClassNode classNode = TransformerBootstrapSupport.toClassNode(classBytecode);
        ClassTransformationReport report = SUPPORT.transformClassNode(className, classNode);
        if (report.isModified()) {
            LOGGER.info("Transformed Forge v1 target " + className + " with " + report.getAppliedTransformerNames());
        }
        return report.isModified() ? report.getTransformedBytecode() : classBytecode;
    }

    private static Path resolveRuntimeDirectory(Map<String, Object> data) {
        Object mcLocation = data.get("mcLocation");
        if (mcLocation instanceof File) {
            return ((File) mcLocation).toPath();
        }

        return Paths.get(".");
    }

    private static LaunchClassLoader resolveLaunchClassLoader(Map<String, Object> data) {
        Object classLoader = data.get("classLoader");
        if (classLoader instanceof LaunchClassLoader) {
            return (LaunchClassLoader) classLoader;
        }

        throw new IllegalStateException("Forge did not provide a LaunchClassLoader in injectData");
    }
}
