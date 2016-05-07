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
	
	public static File minecraftDataFolder=null;
	private static final String MINECRAFT_DATA_FOLDER=".minecraft";
	public static File getMinecraftDataDir(){
		if(minecraftDataFolder!=null)
			return minecraftDataFolder;
		testProbe();
		if(minecraftDataFolder!=null)
			return minecraftDataFolder;
		File temp=new File("");
		if(temp.getAbsolutePath().endsWith(MINECRAFT_DATA_FOLDER))
			return temp;
		File temp0=new File(MINECRAFT_DATA_FOLDER);
		if(temp0.exists())
			return temp0;
		return temp;
	}
	
	private static String minecraftVersion=null;
	public static String getMinecraftVersion(){
		if(minecraftVersion!=null)
			return minecraftVersion;
		testProbe();
		return minecraftVersion;
	}
	
	private final static Pattern MINECRAFT_CORE_FILE_PATTERN = Pattern.compile("^(.*?)/versions/(.*?)/(.*?).jar$");
	public static void testProbe(){
		URLClassLoader ucl = (URLClassLoader)new MinecraftUtil().getClass().getClassLoader();
		URL urls[] = ucl.getURLs();
		for(URL url:urls){
			Matcher m = MINECRAFT_CORE_FILE_PATTERN.matcher(url.getPath());
			if(!m.matches())
				continue;
			if(minecraftDataFolder==null){
				try{
					minecraftDataFolder=new File(url.getPath()).getParentFile().getParentFile().getParentFile();
				}catch(Exception e){
					minecraftDataFolder=null;
				}
			}
			System.out.println(url.getPath()+" "+minecraftDataFolder.getAbsolutePath());
			if(minecraftVersion==null)
				minecraftVersion=m.group(2);
			break;
		}
	}
}
