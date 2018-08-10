package customskinloader.utils;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import net.minecraft.client.renderer.texture.TextureManager;

/**
 * @author Alexander Xia
 * @since 13.6
 *
 */
public class MinecraftUtil {
    public static File getMinecraftDataDir(){
        return Minecraft.getMinecraft().gameDir;
    }
    public static MinecraftSessionService getSessionService(){
        return net.minecraft.client.Minecraft.getMinecraft().getSessionService();
    }
    public static TextureManager getTextureManager(){
        return net.minecraft.client.Minecraft.getMinecraft().getTextureManager();
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
    
    // (domain|ip)(:port)
    public static String getServerAddress(){
        net.minecraft.client.multiplayer.ServerData data=net.minecraft.client.Minecraft.getMinecraft().getCurrentServerData();
        if(data==null)//Single Player
            return null;
        return data.serverIP;
    }
    // ip:port
    public static String getStandardServerAddress(){
        return HttpUtil0.parseAddress(getServerAddress());
    }
    public static boolean isLanServer(){
        return HttpUtil0.isLanServer(getStandardServerAddress());
    }
    public static String getCurrentUsername(){
        return net.minecraft.client.Minecraft.getMinecraft().getSession().getProfile().getName();
    }
    
    private final static Pattern MINECRAFT_CORE_FILE_PATTERN = Pattern.compile("^(.*?)/versions/([^\\/\\\\]*?)/([^\\/\\\\]*?).jar$");
    private static void testProbe(){
        minecraftVersion.clear();
        URL urls[] = JavaUtil.getClasspath();
        for(URL url:urls){
            Matcher m = null;
            try{
                m=MINECRAFT_CORE_FILE_PATTERN.matcher(URLDecoder.decode(url.getPath(),"UTF-8"));
            }catch(Exception e){
                e.printStackTrace();
            }
            if(m==null || !m.matches())
                continue;
            minecraftVersion.add(m.group(2));
        }
    }
    
    public static boolean isCoreFile(URL url){
        Matcher m;
        try{
            m=MINECRAFT_CORE_FILE_PATTERN.matcher(URLDecoder.decode(url.getPath(),"UTF-8"));
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return m.matches();
    }
    
    private final static Pattern LIBRARY_FILE_PATTERN = Pattern.compile("^(.*?)/libraries/(.*?)/([^\\/\\\\]*?).jar$");
    public static boolean isLibraryFile(URL url){
        Matcher m;
        try{
            m=LIBRARY_FILE_PATTERN.matcher(URLDecoder.decode(url.getPath(),"UTF-8"));
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return m.matches();
    }
    
    public static String getCredential(GameProfile profile) {
        return (profile==null || profile.hashCode()==0) ? null : 
            (profile.getId()==null ? profile.getName() : String.format("%s-%s", profile.getName(),profile.getId()));
    }
}
