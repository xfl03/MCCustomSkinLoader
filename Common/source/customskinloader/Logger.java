package customskinloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Logger {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public enum Level{
        DEBUG("DEBUG",false),
        INFO("INFO",true),
        WARNING("WARNING",true);
        
        
        String name;
        boolean display;
        Level(String name,boolean display){
            this.name=name;
            this.display=display;
        }
        public String getName(){
            return name;
        }
        public boolean display(){
            return display;
        }
    }
    private BufferedWriter writer=null;
    public Logger(){
        //Logger isn't created.
    }
    public Logger(String logFile) {
        this(new File(logFile));
    }
    public Logger(File logFile){
        try {
            if(!logFile.getParentFile().exists()){
                logFile.getParentFile().mkdirs();
            }
            if(!logFile.exists())
                logFile.createNewFile();
            
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(logFile), "UTF-8"));

            System.out.println("Log Path: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void close(){
        if(writer!=null){
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void log(Level level,String msg){
        if(!level.display()&&writer==null)
            return;
        String sb = String.format("[%s %s] %s", Thread.currentThread().getName(), level.getName(), msg);
        if(level.display())
            System.out.println(sb);
        if(writer==null)
            return;
        try {
            String sb2 = String.format("[%s] %s\r\n", DATE_FORMAT.format(new Date()), sb);
            writer.write(sb2);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void debug(String msg){
        log(Level.DEBUG,msg);
    }
    public void debug(String format, Object... objs) {
        debug(String.format(format, objs));
    }
    public void info(String msg){
        log(Level.INFO,msg);
    }
    public void info(String format, Object... objs){
        info(String.format(format, objs));
    }
    public void warning(String msg){
        log(Level.WARNING,msg);
    }
    public void warning(String format, Object... objs){
        warning(String.format(format, objs));
    }
    public void warning(Throwable e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log(Level.WARNING,"Exception: "+sw.toString());
    }
}
