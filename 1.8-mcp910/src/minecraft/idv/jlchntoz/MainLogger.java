package idv.jlchntoz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class MainLogger {
	private static final int LOWEST_DISPLAY_LEVEL=Level.INFO.intValue();
	SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private BufferedWriter writer;
	public MainLogger(File logFile){
		try {
			if(!logFile.getParentFile().exists()){
				logFile.getParentFile().mkdirs();
			}
			if(!logFile.exists())
				logFile.createNewFile();
			
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(logFile), "UTF-8"));
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
		if(level.intValue()>=LOWEST_DISPLAY_LEVEL) 
			System.out.println(msg);
		try {
			writer.write("[");
			writer.write(DATE_FORMAT.format(new Date()));
			writer.write(" ");
			writer.write(Thread.currentThread().getName());
			writer.write(" ");
			writer.write(level.getName());
			writer.write("] ");
			writer.write(msg);
			writer.write("\r\n");
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void info(String msg){
		log(Level.INFO,msg);
	}
	public void warning(String msg){
		log(Level.WARNING,msg);
	}
	public void warning(Exception e){
		log(Level.WARNING,"Exception occurs while running: "+e.getMessage());
		StackTraceElement[] stes=e.getStackTrace();
		for(StackTraceElement ste : stes){
			log(Level.WARNING,ste.toString());
		}
		log(Level.WARNING,"");
	}
}
