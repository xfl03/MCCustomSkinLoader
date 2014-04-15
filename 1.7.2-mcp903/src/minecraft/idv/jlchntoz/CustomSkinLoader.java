package idv.jlchntoz;

import net.minecraft.client.Minecraft;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import java.util.regex.*;

/**
 * Custom skin loader mod for Minecraft.
 * 
 * @version 9th Revision 2014.4.15
 * @author (C) Jeremy Lam [JLChnToZ] 2013 & Alexander Xia [xfl03] 2014
 */
public class CustomSkinLoader {
	public final static String DefaultSkinURL = "http://skins.minecraft.net/MinecraftSkins/*.png";
	public final static String DefaultCloakURL = "http://skins.minecraft.net/MinecraftCloaks/*.png";
	private final static Pattern newURLPattern = Pattern.compile("^http://skins.minecraft.net/Minecraft(Skin|Cloak)s/(.*?).png$"),
                                 oldURLPattern = Pattern.compile("^http://s3.amazonaws.com/Minecraft(Skin|Cloak)s/(.*?).png$");

	private final static Logger logger = Logger
			.getLogger(CustomSkinLoader.class.getName());

	private static String[] cloakURLs = null, skinURLs = null;

	private HttpURLConnection C = null;

	public InputStream getPlayerSkinStream(String path) {
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
		}
		return getStream(path, false); // Neither skin nor cloak...
	}

	public InputStream getPlayerSkinStream(Boolean isCloak, String playerName) {
		if (skinURLs == null || cloakURLs == null || skinURLs.length <= 0
				|| cloakURLs.length <= 0)
			refreshSkinURL(); // If the list is blank or null, try to load
								// again.
		for (String l : isCloak ? cloakURLs : skinURLs) {
			String loc = str_replace("*", playerName, l);
			logger.log(Level.INFO, "Try to load " + (isCloak ? "cloak" : "skin") + " in " + loc);
			InputStream S = getStream(loc, true);
			if (S == null)
				logger.log(Level.INFO, "No " + (isCloak ? "cloak" : "skin")
						+ " found in " + loc);
			else
				return S;
		}
		logger.log(Level.INFO, "Try to load skin in default URL instead.");
		return getStream(str_replace("*", playerName, isCloak ? DefaultCloakURL : DefaultSkinURL), true);
	}

	private InputStream getStream(String URL, Boolean CheckPNG) {
		try {
			URL U = new URL(URL);
			C = (HttpURLConnection) U.openConnection();
			C.setDoInput(true);
			C.setDoOutput(false);
			C.connect();
			int respcode = C.getResponseCode() / 100;
			if (respcode != 4 && respcode != 5) { // Successful (?) to get skin.
				BufferedInputStream IS = new BufferedInputStream(
						C.getInputStream());
				if (!CheckPNG) // If no need to check PNG header, just skip it.
					return IS;
				IS.mark(0);
				byte[] ib = new byte[4];
				IS.read(ib);
				if (ib[1] == (byte) 'P' && ib[2] == (byte) 'N'
						&& ib[3] == (byte) 'G') { // Check PNG Header if needed
					IS.reset();
					return IS;
				}
			}
		} catch (Exception ex) {
			logger.log(Level.WARNING, ex.getMessage());
		} finally {
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
			File mcdir = Minecraft.getMinecraft().mcDataDir;
			skinURLs = readAllLines(mcdir, "skinurls.txt");
			cloakURLs = readAllLines(mcdir, "capeurls.txt");
		} catch (Exception ex) {
			logger.log(Level.WARNING, ex.getMessage());
		} finally {
			logger.log(Level.INFO, "Skin URLs Refreshed. Skin count = "
					+ skinURLs.length + ",  Cloak count = " + cloakURLs.length);
		}
	}

	private static String[] readAllLines(File mcdir, String path) {
		try {
			File F = new File(mcdir, path);
			logger.log(Level.INFO, "Config file: " + F.getAbsolutePath());
			if (!F.exists()) {
				logger.log(Level.INFO, "Config file not found, create new one.");
				F.createNewFile();
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
				return str_replace("\r", "\n",
						str_replace("\r\n", "\n", new String(b))).split("\n");
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
}
