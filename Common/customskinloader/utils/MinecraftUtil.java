package customskinloader.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

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
	public static File getMinecraftDataDir(){
		if(minecraftDataFolder!=null)
			return minecraftDataFolder;
		testProbe();
		if(minecraftDataFolder!=null)
			return minecraftDataFolder;
		return new File("");
	}
	
	private static ArrayList<String> minecraftVersion=new ArrayList<String>();
	private static String minecraftMainVersion=null;
	private final static Pattern MINECRAFT_VERSION_PATTERN = Pattern.compile(".*?(\\d+\\.\\d+[\\.]?\\d*).*?");
	public static ArrayList<String> getMinecraftVersions(){
		if(minecraftVersion!=null&&!minecraftVersion.isEmpty())
			return minecraftVersion;
		testProbe();
		return minecraftVersion;
	}
	public static String getMinecraftVersionText(){
		StringBuilder sb=new StringBuilder();
		for(String version:getMinecraftVersions())
			sb.append(version).append(" ");
		return StringUtils.trim(sb.toString());
	}
	public static String getMinecraftMainVersion(){
		if(minecraftMainVersion!=null)
			return minecraftMainVersion;
		for(String version:getMinecraftVersions()){
			Matcher m = null;
			try{
				m=MINECRAFT_VERSION_PATTERN.matcher(version);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(m==null || !m.matches())
				continue;
			minecraftMainVersion=m.group(m.groupCount());
			break;
		}
		return minecraftMainVersion;
	}
	
	public static String getServerAddress(){
		net.minecraft.client.multiplayer.ServerData data=net.minecraft.client.Minecraft.getMinecraft().getCurrentServerData();
		if(data==null)//Single Player
			return null;
		return data.serverIP;
	}
	public static boolean isLanServer(){
		net.minecraft.client.multiplayer.ServerData data=net.minecraft.client.Minecraft.getMinecraft().getCurrentServerData();
		if(data==null)//Single Player
			return true;
		return HttpUtil0.isLanServer(data.serverIP);
	}
	public static String getCurrentUsername(){
		return net.minecraft.client.Minecraft.getMinecraft().getSession().getProfile().getName();
	}
	
	private final static Pattern MINECRAFT_CORE_FILE_PATTERN = Pattern.compile("^(.*?)/versions/([^\\/\\\\]*?)/([^\\/\\\\]*?).jar$");
	private static void testProbe(){
		minecraftVersion.clear();
		URLClassLoader ucl = (URLClassLoader)new MinecraftUtil().getClass().getClassLoader();
		URL urls[] = ucl.getURLs();
		for(URL url:urls){
			Matcher m = null;
			try{
				m=MINECRAFT_CORE_FILE_PATTERN.matcher(URLDecoder.decode(url.getPath(),"UTF-8"));
			}catch(Exception e){
				e.printStackTrace();
			}
			if(m==null || !m.matches())
				continue;
			if(minecraftDataFolder==null)
				minecraftDataFolder=new File(m.group(1));
			minecraftVersion.add(m.group(2));
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
		return m!=null && m.matches();
	}
	
	private final static Pattern LIBRARY_FILE_PATTERN = Pattern.compile("^(.*?)/libraries/(.*?)/([^\\/\\\\]*?).jar$");
	public static boolean isLibraryFile(URL url){
		Matcher m = null;
		try{
			m=LIBRARY_FILE_PATTERN.matcher(URLDecoder.decode(url.getPath(),"UTF-8"));
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return m!=null && m.matches();
	}
}
