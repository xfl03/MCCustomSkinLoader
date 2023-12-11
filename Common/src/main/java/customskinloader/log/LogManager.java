package customskinloader.log;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 14.16
 */
public class LogManager {
    private static Path logFile;
    private static Writer logWriter;
    private static final Map<String, Logger> loggers = new ConcurrentHashMap<>();

    public static void setLogFile(Path logFile) {
        //Not updated
        if (logFile == null || logFile.equals(LogManager.logFile)) {
            return;
        }
        LogManager.logFile = logFile;
        try {
            //Close previous writer
            if (logWriter != null) {
                logWriter.close();
            }
            //Set new writer
            Files.createDirectories(logFile.getParent());
            logWriter = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            loggers.forEach((k, v) -> v.setWriter(logWriter));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger(String loggerName) {
        return loggers.computeIfAbsent(loggerName,
                it -> new Logger(logWriter, "CustomSkinLoader " + loggerName));
    }

}
