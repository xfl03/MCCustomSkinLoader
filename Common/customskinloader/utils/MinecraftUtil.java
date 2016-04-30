package customskinloader.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
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
	
	public static String getMinecraftVersion(){
		return net.minecraft.client.Minecraft.getMinecraft().getVersion();
	}
	
}
