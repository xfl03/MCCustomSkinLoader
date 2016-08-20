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
	private BufferedWriter writer=null;
	public Logger(){
		//Logger isn't created.
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
		if(level.intValue()<LOWEST_DISPLAY_LEVEL&&writer==null)
			return;
		StringBuilder sb=new StringBuilder();
		sb.append("[").append(Thread.currentThread().getName()).append(" ").append(level.getName()).append("] ");
		sb.append(msg);
		if(level.intValue()>=LOWEST_DISPLAY_LEVEL)
			System.out.println(sb.toString());
		if(writer==null)
			return;
		try {
			StringBuilder sb2=new StringBuilder();
			sb2.append("[").append(DATE_FORMAT.format(new Date())).append("] ");
			sb2.append(sb.toString()).append("\r\n");
			writer.write(sb2.toString());
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
		log(Level.WARNING,"Exception: "+e.toString());
		StackTraceElement[] stes=e.getStackTrace();
		for(StackTraceElement ste : stes){
			log(Level.WARNING,ste.toString());
		}
	}
}
