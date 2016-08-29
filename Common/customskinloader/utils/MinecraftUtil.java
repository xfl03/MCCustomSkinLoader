package customskinloader.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MinecraftUtil for mcp version.
 * It is the only class in package 'customskinloader' which has differences in mcp/non-mcp version.
 * 
 * @author Alexander Xia
 * @since 13.6
 *
 */
public class MinecraftUtil {
	public static File getMinecraftDataDir0(){
		return net.minecraft.client.Minecraft.getMinecraft().mcDataDir;
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
	
	private final static Pattern MINECRAFT_CORE_FILE_PATTERN = Pattern.compile("^(.*?)/versions/([^\\/\\\\]*?)/([^\\/\\\\]*?).jar$");
	private static void testProbe(){
		URLClassLoader ucl = (URLClassLoader)new MinecraftUtil().getClass().getClassLoader();
		URL urls[] = ucl.getURLs();
		for(URL url:urls){
			Matcher m = null;
			try{
				m=MINECRAFT_CORE_FILE_PATTERN.matcher(URLDecoder.decode(url.getPath(),"UTF-8"));
			}catch(Exception e){
				e.printStackTrace();
			}
			if(m==null)
				continue;
			if(!m.matches())
				continue;
			if(minecraftDataFolder==null)
				minecraftDataFolder=new File(m.group(1));
			//System.out.println(url.getPath()+" "+minecraftDataFolder.getAbsolutePath());
			if(minecraftVersion==null)
				minecraftVersion=m.group(2);
			break;
		}
	}
	public static boolean isCoreFile(URL url){
		Matcher m = null;
		try{
			m=MINECRAFT_CORE_FILE_PATTERN.matcher(URLDecoder.decode(url.getPath(),"UTF-8"));
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		if(m==null)
			return false;
		return m.matches();
	}
}
