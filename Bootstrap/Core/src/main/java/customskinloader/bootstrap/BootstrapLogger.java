package customskinloader.bootstrap;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BootstrapLogger {
    public static final Logger LOGGER = LogManager.getLogger("CustomSkinLoader Bootstrap");

    private BootstrapLogger() {
    }

    public static String formatPath(Path path) {
        return path == null ? "<unknown>" : path.toAbsolutePath().normalize().toString();
    }
}
