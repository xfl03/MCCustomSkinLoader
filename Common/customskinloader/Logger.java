package customskinloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class Logger {
	private static final int LOWEST_DISPLAY_LEVEL=Level.INFO.intValue();
	SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private BufferedWriter writer;
	public Logger(File logFile){
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
			StringBuilder sb=new StringBuilder();
			sb.append("[");
			sb.append(DATE_FORMAT.format(new Date()));
			sb.append(" ");
			sb.append(Thread.currentThread().getName());
			sb.append(" ");
			sb.append(level.getName());
			sb.append("] ");
			sb.append(msg);
			sb.append("\r\n");
			writer.write(sb.toString());
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
		log(Level.WARNING,"Exception: "+e.getMessage());
		StackTraceElement[] stes=e.getStackTrace();
		for(StackTraceElement ste : stes){
			log(Level.WARNING,ste.toString());
		}
		log(Level.WARNING,"");
	}
}
