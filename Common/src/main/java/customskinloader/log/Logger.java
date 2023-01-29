package customskinloader.log;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * the Logger
 * Moved from <code>customskinloader.Logger</code>
 *
 * @since 14.16
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class Logger {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * A copy of <code>Config.enableLogStdOut</code>.
     * Because <code>Config.loadConfig0()</code> will use <code>Logger.log()</code>,
     * which means cannot access <code>CustomSkinLoader.config</code> here.
     *
     * @see customskinloader.config.Config#enableLogStdOut
     * @since 14.15
     */
    public boolean enableLogStdOut = false;

    public enum Level {
        DEBUG("DEBUG", false),
        INFO("INFO", true),
        WARNING("WARNING", true);


        final String name;
        final boolean display;

        Level(String name, boolean display) {
            this.name = name;
            this.display = display;
        }

        public String getName() {
            return name;
        }

        public boolean display() {
            return display;
        }
    }

    private Writer writer = null;

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Logger() {
        //Logger isn't created.
    }

    public Logger(String logFile) {
        this(new File(logFile));
    }

    public Logger(File logFile) {
        this(logFile, "Unknown");
    }

    /**
     * Create Logger with File
     *
     * @since 14.16
     */
    public Logger(File logFile, String loggerName) {
        this.loggerName = loggerName;
        try {
            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            writer = new BufferedWriter(new OutputStreamWriter(
                    Files.newOutputStream(logFile.toPath()), StandardCharsets.UTF_8));

            System.out.println("Log Path: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create Logger with special Writer
     *
     * @since 14.16
     */
    public Logger(@Nullable Writer writer, String loggerName) {
        this.loggerName = loggerName;
        this.writer = writer;
    }

    /**
     * Logger name in log
     *
     * @since 14.16
     */
    String loggerName;

    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Judge if a level can be printed to standard output(System.out) with config.
     *
     * @param level log level
     * @return true - print
     */
    private boolean canPrintToStdOut(Level level) {
        return level.display() && enableLogStdOut;
    }

    public void log(Level level, String msg) {
        //When it neither be printed to standard output nor write to file
        if (!canPrintToStdOut(level) && writer == null) {
            return;
        }

        //Format, print and write
        String sb = String.format("[%s] [%s/%s] [%s]: %s",
                DATE_FORMAT.format(new Date()), Thread.currentThread().getName(), level.getName(), loggerName, msg);
        if (canPrintToStdOut(level)) {
            System.out.println(sb);
        }
        if (writer == null) {
            return;
        }
        try {
            String sb2 = String.format("%s\r\n", sb);
            writer.write(sb2);//Writer.write has lock
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void debug(String msg) {
        log(Level.DEBUG, msg);
    }

    public void debug(String format, Object... objs) {
        debug(String.format(format, objs));
    }

    public void info(String msg) {
        log(Level.INFO, msg);
    }

    public void info(String format, Object... objs) {
        info(String.format(format, objs));
    }

    public void warning(String msg) {
        log(Level.WARNING, msg);
    }

    public void warning(String format, Object... objs) {
        warning(String.format(format, objs));
    }

    public void warning(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log(Level.WARNING, "Exception: " + sw);
    }
}
