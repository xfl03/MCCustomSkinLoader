package customskinloader.bootstrap.installer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.mapping.Parser;
import org.apache.logging.log4j.Logger;

public final class CommonJarInstaller {
    private static final String COMMON_JAR = "CustomSkinLoader-Common.jar";
    private static final String MAPPING_RESOURCE = "/customskinloader/mapping.xml";
    private static final Parser TSRG_PARSER = new Parser();
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    private CommonJarInstaller() {
    }

    public static Path releaseCommonJar(Path gameDirectory, String mappingType) throws Exception {
        InputStream mappingStream = CommonJarInstaller.class.getResourceAsStream(MAPPING_RESOURCE);
        if (mappingStream == null) {
            throw new IOException("Could not find bundled mapping resource " + MAPPING_RESOURCE);
        }

        try (InputStream mappingInputStream = mappingStream) {
            Path coreDirectory = gameDirectory.resolve("CustomSkinLoader").resolve("Core");
            Files.createDirectories(coreDirectory);

            Path jarPath = coreDirectory.resolve(COMMON_JAR);
            LOGGER.info("Releasing CustomSkinLoader Common jar to " + BootstrapLogger.formatPath(jarPath));
            String resourceName = "/META-INF/jar-jar/" + COMMON_JAR;
            InputStream jarStream = CommonJarInstaller.class.getResourceAsStream(resourceName);
            if (jarStream == null) {
                throw new IOException("Could not find bundled jar resource " + resourceName);
            }

            try (InputStream jarInputStream = jarStream; OutputStream outputStream = Files.newOutputStream(jarPath)) {
                new NestedJarRemapper(TSRG_PARSER.parse(mappingInputStream, mappingType)).remapJar(jarInputStream, outputStream);
            }
            return jarPath;
        }
    }
}
