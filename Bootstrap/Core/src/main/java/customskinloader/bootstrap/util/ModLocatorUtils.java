package customskinloader.bootstrap.util;

import java.lang.reflect.Field;
import java.nio.file.Path;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.installer.CommonJarInstaller;
import org.apache.logging.log4j.Logger;

/**
 * Shared setup for Forge-style mods-folder locators that expose the released jar as a synthetic mod.
 */
public final class ModLocatorUtils {
    public static final String LOCATOR_NAME = "customskinloader-locator";
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    private ModLocatorUtils() {
    }

    public static void initialize(Object locator, Class<?> locatorType, Path gameDirectory, String mappingType) {
        try {
            Path runtimeDirectory = resolveRuntimeDirectory(gameDirectory);
            LOGGER.info("Initializing " + LOCATOR_NAME + " for " + mappingType + " mappings in " + BootstrapLogger.formatPath(gameDirectory));
            setModFolder(locator, locatorType, runtimeDirectory);
            Path commonJar = CommonJarInstaller.releaseCommonJar(gameDirectory, mappingType);
            LOGGER.info("Initialized " + LOCATOR_NAME + " with mod folder " + BootstrapLogger.formatPath(runtimeDirectory)
                + " and Common jar " + BootstrapLogger.formatPath(commonJar));
        } catch (Exception exception) {
            LOGGER.error("Failed to prepare CustomSkinLoader runtime artifacts for discovery", exception);
            throw new IllegalStateException("Failed to prepare CustomSkinLoader runtime artifacts for discovery", exception);
        }
    }

    private static Path resolveRuntimeDirectory(Path gameDirectory) {
        return gameDirectory.resolve("CustomSkinLoader").resolve("Core");
    }

    private static void setModFolder(Object locator, Class<?> locatorType, Path modFolder) throws NoSuchFieldException, IllegalAccessException {
        Field field = locatorType.getDeclaredField("modFolder");
        field.setAccessible(true);
        field.set(locator, modFolder);
    }
}
