package idv.jlchntoz;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.*;
import java.util.logging.*;
import java.util.regex.*;

/**
 * Custom skin loader mod for Minecraft.
 * 
 * @version 12th Revision 2nd Subversion 2015.6.7
 * @author (C) Jeremy Lam [JLChnToZ] 2013 & Alexander Xia [xfl03] 2014-2015
 */
public class CustomSkinLoader {
	public final static String VERSION="12.2";
	
	public final static String DefaultSkinURL = "http://skins.minecraft.net/MinecraftSkins/*.png";
	public final static String DefaultCloakURL = "http://skins.minecraft.net/MinecraftCloaks/*.png";
	private final static Pattern newResPattern = Pattern.compile("^http://textures.minecraft.net/texture/(.*?)?(Skin|Cloak)=(.*?)$"),
								 newURLPattern = Pattern.compile("^http://skins.minecraft.net/Minecraft(Skin|Cloak)s/(.*?).png$"),
								 oldURLPattern = Pattern.compile("^http://s3.amazonaws.com/Minecraft(Skin|Cloak)s/(.*?).png$");
	
	private final static File DATA_DIR=new File(Minecraft.getMinecraft().mcDataDir,"CustomSkinLoader");
	private final static File SKIN_DIR=new File(DATA_DIR,"skins");
	private final static File CLOAK_DIR=new File(DATA_DIR,"cloaks");
	private final static MainLogger logger = new MainLogger(new File(DATA_DIR,"CustomSkinLoader.log"));
	
	private static String[] cloakURLs = null, skinURLs = null;
	private HttpURLConnection C = null;
	private boolean loadFromCache=false;

	public InputStream getPlayerSkinStream(String path) {
		if(!DATA_DIR.exists())
			DATA_DIR.mkdir();
		logger.info("Get a request: "+path);
		Matcher m = newURLPattern.matcher(path);
		if (!m.matches())//Is not new url
        {
            m = oldURLPattern.matcher(path);//If is old url
        }
		if (m.matches()) {
			if (m.group(1).contains("Skin")) // Skin
				return getPlayerSkinStream(false, m.group(2));
			else if (m.group(1).contains("Cloak")) // Cloak
				return getPlayerSkinStream(true, m.group(2));
		}else{
			m = newResPattern.matcher(path);
			if(m.matches()){
				logger.info("Matches");
				if (m.group(2).contains("Skin")) // Skin
					return getPlayerSkinStream(false, m.group(3));
				else if (m.group(2).contains("Cloak")) // Cloak
					return getPlayerSkinStream(true, m.group(3));
			}
		}
		return getStream(path, false); // Neither skin nor cloak...
	}

	public InputStream getPlayerSkinStream(Boolean isCloak, String playerName) {
		if (skinURLs == null || cloakURLs == null || skinURLs.length <= 0
				|| cloakURLs.length <= 0)
			refreshSkinURL(); // If the list is blank or null, try to load again.
		InputStream S=null;
		File temp=null;
		if(isCloak)
			temp=new File(CLOAK_DIR,playerName+".png");
		else
			temp=new File(SKIN_DIR,playerName+".png");
		for (String l : isCloak ? cloakURLs : skinURLs) {
			if(l==null||l.equalsIgnoreCase(""))
				continue;
			String loc = str_replace("*", playerName, l);
			logger.log(Level.INFO, "Try to load " + (isCloak ? "cloak" : "skin") + " in " + loc);
			long last=0;
			if(temp.exists()&&temp.length()>0){
				last=temp.lastModified();
			}
			S = getStream(loc, true,last);
			if(loadFromCache){
				logger.info("" + (isCloak ? "Cloak" : "Skin") + " in " + loc + "will be load from cache.");
				break;
			}
			if (S == null){
				logger.log(Level.INFO, "No " + (isCloak ? "cloak" : "skin")
						+ " found in " + loc);
			}else{
				logger.info("Succeessfully load " + (isCloak ? "cloak" : "skin") + " in " + loc);
				break;
			}
		}
		if(S==null){
			try{//Read Local Skin File
				if(temp.exists() && temp.length()>1){
					logger.info("Try load local " + (isCloak ? "cloak" : "skin") + " in " + temp.getAbsolutePath());
					InputStream in = new FileInputStream(temp);
					BufferedInputStream bis=new BufferedInputStream(in);
					if(bis.available()<=0){
						logger.info("Cannot load local " + (isCloak ? "cloak" : "skin") + " in " + temp.getAbsolutePath());
					}else{
						logger.info("Successfully load " + (isCloak ? "cloak" : "skin") + " in " + temp.getAbsolutePath());
						return bis;
					}
				}else{
					logger.info("No local " + (isCloak ? "cloak" : "skin") + " found in " + temp.getAbsolutePath());
				}
			}catch(Exception e){
				logger.log(Level.WARNING, e.getMessage());
			}
		}else{
			String user= Minecraft.getMinecraft().getSession().getUsername();
			if(user.equalsIgnoreCase(playerName)){//Only save user's skin
				logger.info("Try save local " + (isCloak ? "cloak" : "skin") + " to " + temp.getAbsolutePath());
				FileOutputStream fs=null;
				int times=0;
				try{//Save to Local Skin File
					//logger.info(user);
					if(!temp.getParentFile().exists())
						temp.getParentFile().mkdir();
					else if(temp.exists()){
						temp.delete();
					}
					//temp.createNewFile();
					fs = new FileOutputStream(temp);
					int byteRead = 0;
					byte[] buffer = new byte[1024];
					//S.reset();
					while (( byteRead = S.read(buffer)) != -1) {
						times+=byteRead;
						//logger.info(""+S.available());
						fs.write(buffer, 0, byteRead);
						if(times>=C.getContentLength()){
							logger.info(times+" "+C.getContentLength());
							break;
						}
					}
					
					if(temp.length()>1){
						logger.info("Successfully save " + (isCloak ? "cloak" : "skin") + " to " + temp.getAbsolutePath());
					}else{
						temp.delete();
						logger.info("Cannot save local " + (isCloak ? "cloak" : "skin") + " to " + temp.getAbsolutePath());
					}
				}catch(Exception e){
					e.printStackTrace();
					logger.info(times+"");
					logger.warning(e);
				}finally{
					try {
						if(fs!=null)
							fs.close();
						S.reset();
						logger.info((isCloak ? "Cloak" : "Skin")+" size : "+S.available());
					} catch (IOException e) {
						logger.warning(e);
					}
				}
			}
			
			return S;
		}
		
		logger.log(Level.INFO, "Try to load skin in default URL instead.");
		return getStream(str_replace("*", playerName, isCloak ? DefaultCloakURL : DefaultSkinURL), true);
	}
	private InputStream getStream(String URL, Boolean CheckPNG) {
		return getStream(URL,CheckPNG,0);
	}
	private InputStream getStream(String URL, Boolean CheckPNG,long last) {
		boolean success=false;
		try {
			URL U = new URL(URL);
			C = (HttpURLConnection) U.openConnection();
			C.setDoInput(true);
			C.setDoOutput(false);
			if(last>0)
				C.setIfModifiedSince(last);
			C.connect();
			logger.info("RC: "+C.getResponseCode());
			int respcode = C.getResponseCode() / 100;
			if (respcode != 4 && respcode != 5) { // Successful (?) to get skin.
				//logger.info(last+" "+C.getLastModified());
				if(C.getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED){//304
					logger.info("Not Modified!");
					loadFromCache=true;
					return null;
				}
				if(C.getContentLength()<=0){
					return null;
				}
				InputStream IS=new BufferedInputStream(C.getInputStream());
				if (!CheckPNG){ // If no need to check PNG header, just skip it.
					success=true;
					return IS;
				}
				//logger.info(C.getContentLength()+" "+IS.available());
				IS.mark(C.getContentLength()+10);
				byte[] ib = new byte[4];
				IS.read(ib);
				if (ib[1] == (byte) 'P' && ib[2] == (byte) 'N'
						&& ib[3] == (byte) 'G') { // Check PNG Header if needed
					IS.reset();
					success=true;
					return IS;
				}
			}
		} catch (Exception ex) {
			success=false;
			logger.log(Level.WARNING, ex.getMessage());
		}finally{
			if(!success)
				disconnect();
		}
		return null;
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
}
