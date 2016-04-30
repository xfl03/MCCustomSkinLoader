package customskinloader.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MinecraftUtil for non-mcp version.
 * It is the only class in package 'customskinloader' which has differences in mcp/non-mcp version.
 * 
 * @author Alexander Xia
 * @since 13.6
 *
 */
public class MinecraftUtil {
	public static File getMinecraftDataDir0(){
		return getMinecraftDataDir();
	}
	
	private static String MINECRAFT_DATA_FOLDER=".minecraft";
	public static File getMinecraftDataDir(){
		File temp=new File("");
		if(temp.getAbsolutePath().endsWith(MINECRAFT_DATA_FOLDER))
			return temp;
		File temp0=new File(MINECRAFT_DATA_FOLDER);
		if(temp0.exists())
			return temp0;
		return temp;
	}
	
	private final static Pattern MINECRAFT_CORE_FILE_PATTERN = Pattern.compile("^(.*?)/versions/(.*?)/(.*?).jar$");
	public static String getMinecraftVersion(){
		URLClassLoader ucl = (URLClassLoader)new MinecraftUtil().getClass().getClassLoader();
		URL urls[] = ucl.getURLs();
		for(URL url:urls){
			Matcher m = MINECRAFT_CORE_FILE_PATTERN.matcher(url.getPath());
			//System.out.println(m.matches()+" "+url.getPath());
			if(!m.matches())
				continue;
			return m.group(2);
		}
		return "Unknown Version";
	}
	
}
