package idv.jlchntoz;

import net.minecraft.src.Minecraft;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.*;
import java.util.regex.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Custom skin loader mod for Minecraft.
 * 
 * @version 12th Revision 7th Subversion 2015.8.22
 * 
 * @author (C) Jeremy Lam [JLChnToZ] 2013 & Alexander Xia [xfl03] 2014-2015
 */
public class CustomSkinLoader {
	public final static String VERSION="12.7";
	
	public final static String DefaultSkinURL = "http://skins.minecraft.net/MinecraftSkins/*.png";
	public final static String DefaultCloakURL = "http://skins.minecraft.net/MinecraftCloaks/*.png";
	
	private final static Pattern newResPattern = Pattern.compile("^http://textures.minecraft.net/texture/(.*?)?(Skin|Cloak)=(.*?)$"),
                                 newURLPattern = Pattern.compile("^http://skins.minecraft.net/Minecraft(Skin|Cloak)s/(.*?).png$"),
                                 oldURLPattern = Pattern.compile("^http://s3.amazonaws.com/Minecraft(Skin|Cloak)s/(.*?).png$"),
                                 optifineCapeURLPattern = Pattern.compile("^http://s.optifine.net/capes/(.*?).png$");
	
	public final static File DATA_DIR=new File(Minecraft.getMinecraft().mcDataDir,"CustomSkinLoader"),
	                         CACHE_DIR=new File(DATA_DIR,"caches"),
	                         LOG_FILE=new File(DATA_DIR,"CustomSkinLoader.log");
	public final static MainLogger logger = getLogger(LOG_FILE);
	
	private static String[] cloakURLs = null, skinURLs = null;
	private HttpURLConnection C = null;
	
	//Entrance
	public InputStream getPlayerSkinStream(String path) {
		if(!DATA_DIR.exists())
			DATA_DIR.mkdir();
		if(!CACHE_DIR.exists())
			CACHE_DIR.mkdir();
		logger.info("Get a request: "+path);
		Matcher m = newURLPattern.matcher(path);
		if (!m.matches())//Is not new url
        {
            m = oldURLPattern.matcher(path);//May be old url
        }
		if (m.matches()) {
			if (m.group(1).contains("Skin")) // Skin
				return getPlayerSkinStream(false, m.group(2));
			else if (m.group(1).contains("Cloak")) // Cloak
				return getPlayerSkinStream(true, m.group(2));
		}else{
			m = newResPattern.matcher(path);
			if(m.matches()){
				if (m.group(2).contains("Skin")) // Skin
					return getPlayerSkinStream(false, m.group(3));
				else if (m.group(2).contains("Cloak")) // Cloak
					return getPlayerSkinStream(true, m.group(3));
			}else{
				m = optifineCapeURLPattern.matcher(path);
				if(m.matches()){//Optifine Cape
					logger.info("Ignore Optifine Cape.");
					return null;//Not Load This
				}
			}
		}
		return getStream(path, false); // Neither skin nor cloak...
	}

	public InputStream getPlayerSkinStream(Boolean isCloak, String playerName) {
		if (skinURLs == null || cloakURLs == null || skinURLs.length <= 0
				|| cloakURLs.length <= 0)
			refreshSkinURL(); // If the list is blank or null, try to load again.
		InputStream S=null;
		
		for (String l : isCloak ? cloakURLs : skinURLs) {
			if(l==null||l.equalsIgnoreCase(""))
				continue;
			String loc = str_replace("*", playerName, l);
			logger.log(Level.INFO, "Try to load " + (isCloak ? "cloak" : "skin") + " in " + loc);
			S = getStream(loc, true);
			
			if (S == null){
				logger.log(Level.INFO, "No " + (isCloak ? "cloak" : "skin") + " found in " + loc);
			}else{
				logger.info("Succeessfully load " + (isCloak ? "cloak" : "skin") + " in " + loc);
				return S;
			}
		}
		
		logger.log(Level.INFO, "Try to load skin in default URL instead.");
		return getStream(str_replace("*", playerName, isCloak ? DefaultCloakURL : DefaultSkinURL), true);
	}
	
	private InputStream getStream(String URL, Boolean CheckPNG) {
		boolean success=false;
		File cacheFile=new File(CACHE_DIR,MD5(URL));
		boolean alreadyCached=false;
		if(cacheFile.exists()&&cacheFile.length()>1){
			alreadyCached=true;
			logger.info("Cache File Found");
		}
		try {
			URL U = new URL(URL);
			C = (HttpURLConnection) U.openConnection();
			C.setReadTimeout(15000);
			C.setDoInput(true);
			C.setDoOutput(false);
			C.connect();
			logger.info("Response Code: "+C.getResponseCode());
			int respcode = C.getResponseCode() / 100;
			if (respcode != 4 && respcode != 5) { // Successful to get skin.
				if(alreadyCached && C.getLastModified()==cacheFile.lastModified()){
					logger.info("Not Modified!");
					return getStreamFromCache(cacheFile);
				}
				if(C.getContentLength()<=0){//Nothing to get
					return null;
				}
				InputStream IS=new BufferedInputStream(C.getInputStream());
				if (!CheckPNG){ // If no need to check PNG header, just skip it.
					success=true;
					saveToCache(IS,cacheFile);
					return IS;
				}
				//logger.info(C.getContentLength()+" "+IS.available());
				IS.mark(C.getContentLength()+10);
				byte[] ib = new byte[4];
				IS.read(ib);
				if (ib[1] == (byte) 'P' && ib[2] == (byte) 'N' && ib[3] == (byte) 'G') { // Check PNG Header if needed
					IS.reset();
					success=true;
					saveToCache(IS,cacheFile);
					return IS;
				}
			}
		} catch (Exception ex) {
			success=false;
			logger.log(Level.WARNING, ex.getMessage());
			if(alreadyCached)
				return getStreamFromCache(cacheFile);
		}finally{
			if(!success)
				disconnect();
		}
		return null;
	}
	private InputStream getStreamFromCache(File cacheFile) {
		try{
			logger.info("Try load local cache in " + cacheFile.getAbsolutePath());
			InputStream in = new FileInputStream(cacheFile);
			BufferedInputStream bis=new BufferedInputStream(in);
			if(bis.available()<=0){
				logger.info("Cannot load local cache in " + cacheFile.getAbsolutePath());
				return null;
			}else{
				logger.info("Successfully load cache in " + cacheFile.getAbsolutePath());
				return bis;
			}
		}catch(Exception e){
			logger.log(Level.WARNING, e.getMessage());
			return null;
		}
	}
	private void saveToCache(InputStream is,File to){
		logger.info("Try save local cache to " + to.getAbsolutePath());
		FileOutputStream fs=null;
		int times=0;
		try{//Save to Local Skin File
			if(to.exists()){
				to.delete();
			}
			fs = new FileOutputStream(to);
			int byteRead = 0;
			byte[] buffer = new byte[1024];
			while (( byteRead = is.read(buffer)) != -1) {
				times+=byteRead;
				fs.write(buffer, 0, byteRead);
				if(times>=C.getContentLength()){
					//logger.info(times+" "+C.getContentLength());
					break;
				}
			}
			
			if(to.length()>1){
				logger.info("Successfully save cache to " + to.getAbsolutePath());
			}else{
				to.delete();
				logger.info("Cannot save local cache to " + to.getAbsolutePath());
			}
		}catch(Exception e){
			logger.warning(e.getMessage());
		}finally{
			try {
				if(fs!=null)
					fs.close();
				is.reset();
				logger.info("Cache size : "+is.available());
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
			if(to.exists()){
				to.setLastModified(C.getLastModified());
			}
		}
	}
	
	public void disconnect() {
		if (C != null)
			C.disconnect();
	}

	private static void refreshSkinURL() {
		try {
			skinURLs = readAllLines(DATA_DIR, "skinurls.txt");
			cloakURLs = readAllLines(DATA_DIR, "capeurls.txt");
		} catch (Exception ex) {
			logger.log(Level.WARNING, ex.getMessage());
		} finally {
			if(skinURLs.length==0&&cloakURLs.length==0){
				logger.info("No skinURLs and cloak URLS found, try to show GUI.");
				showGUI();
			}else{
				logger.log(Level.INFO, "Skin URLs Refreshed. Skin count = "
						+ skinURLs.length + ",  Cloak count = " + cloakURLs.length);
			}
		}
	}

	private static String[] readAllLines(File dir, String path) {
		try {
			File F = new File(dir, path);
			logger.log(Level.INFO, "Config file: " + F.getAbsolutePath());
			if (!F.exists()) {
				logger.log(Level.INFO, "Config file not found.");
				return new String[0];
			} else if (F.length() <= 0) {
				logger.log(Level.INFO, "Config file is blank, skipped.");
				return new String[0];
			} else {
				byte[] b = new byte[(int) F.length()];
				BufferedInputStream S = new BufferedInputStream(
						new FileInputStream(F));
				S.read(b);
				S.close();
				logger.log(Level.INFO, "Config file loaded.");
				String[] re= str_replace("\r", "\n",
						str_replace("\r\n", "\n", new String(b))).split("\n");
				for(int i=0;i<re.length;i++){
					if(re[i].startsWith("#"))
						re[i]=null;
					else{
						for(int g=0;g<re.length;g++){
							if(i==g)
								continue;
							if(re[i].equalsIgnoreCase(re[g])){
								re[i]=null;
								break;
							}
						}
					}
				}
				return re;
			}
		} catch (Exception ex) {
			logger.log(Level.WARNING, ex.getMessage());
			return new String[0];
		}
	}

	private static String str_replace(String search, String replace,
			String subject) {
		StringBuffer result = new StringBuffer(subject);
		int pos;
		while ((pos = result.indexOf(search)) != -1)
			result.replace(pos, pos + search.length(), replace);
		return result.toString();
	}
	
	public static void showGUI(){
		try{
			Class clazz=Class.forName("idv.jlchntoz.CustomSkinLoaderGUI");
			String[] arg={"f",VERSION,"in"};
			CustomSkinLoaderGUI.main(arg);
			return;
		}catch(Exception e){
			logger.warning(e.getMessage());
		}
		
		File a=new File(DATA_DIR,"CustomSkinLoaderGUI.jar");
		if(!a.exists()||a.length()<1){
			a.delete();
			logger.info("No GUI file found, try to download one to "+a.getAbsolutePath());
			downloadFile("https://raw.githubusercontent.com/JLChnToZ/MCCustomSkinLoader/GUI/CustomSkinLoaderGUI.jar",a);
		}
		try{
			String toRun="java -jar \""+a.getAbsolutePath()+"\" f "+VERSION;
			logger.info("Run: "+toRun);
			Runtime.getRuntime().exec(toRun);
		}catch(Exception e){
			logger.warning(e.getMessage());
		}
	}
	public static boolean downloadFile(String remote,File local){
		try {
			File LCK=new File(local.getParentFile(),"download.lck");
			if(LCK.exists()&&LCK.lastModified()>=System.currentTimeMillis()-10000){
				logger.info("'download.lck' found! Download will not start.");
				return false;
			}
			LCK.createNewFile();
			logger.info("Downloading "+remote+" to "+local.getAbsolutePath());
			URL url = new URL(remote);
			URLConnection conn = url.openConnection();
			InputStream inStream = conn.getInputStream();
			FileOutputStream fs = new FileOutputStream(local);
			int byteRead = 0;
			byte[] buffer = new byte[1024];
			while (( byteRead = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteRead);
			}
			fs.close();
			LCK.delete();
			logger.info("Download successfully!");
			return local.exists();
		}catch (Exception e) {
			logger.warning(e.getMessage());
			return false;
		}
	}
	public String MD5(String str){
		byte [] buf = str.getBytes();
        MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.update(buf);
	        byte [] tmp = md5.digest();
	        StringBuilder sb = new StringBuilder();
	        for (byte b:tmp) {
	            sb.append(Integer.toHexString(b&0xff));
	        }
	        return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			logger.warning(e.getMessage());
		}
		return "Fail";
	}
	private static MainLogger getLogger(File logFile){
		MainLogger mainLogger = new MainLogger(logFile);
		mainLogger.info("CustomSkinLoader " + VERSION);
		return mainLogger;
	}
}
