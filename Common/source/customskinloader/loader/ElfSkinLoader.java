package customskinloader.loader;

import java.util.HashMap;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.StringUtils;

import com.mojang.authlib.GameProfile;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.ProfileLoader.IProfileLoader;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpRequestUtil.HttpRequest;
import customskinloader.utils.HttpRequestUtil.HttpResponce;
import customskinloader.utils.MinecraftUtil;

//For ElfSkin http://www.mcelf.com/s/mcskin
public class ElfSkinLoader implements IProfileLoader {
	private static final String LOGIN_URL="http://status.mcelf.com/login?gid=%SERVER_IP%&name=%USERNAME%&oid=%ELF_ID%";
	private static final String LOGOUT_URL="http://status.mcelf.com/logout?gid=%SERVER_IP%&name=%USERNAME%";
	private static final String PROFILE_URL="http://status.mcelf.com/s?gid=%SERVER_IP%&name=%USERNAME%";
	
	private static String elfID=Preferences.userRoot().node("elfskin").get("elfid", "null");
	private static String lastLoginServer=null;
	private static HashMap<String,Profile> cache=new HashMap<String, Profile>();
	@Override
	public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
		String username=gameProfile.getName();
		String ip=MinecraftUtil.getServerAddress();
		if(elfID!=null && isIPChange(ip)){
			if(lastLoginServer!=null)
				makeLogout(lastLoginServer,MinecraftUtil.getCurrentUsername());
			Profile[] ps=makeLogin(ip,MinecraftUtil.getCurrentUsername(),elfID);
			cache.clear();
			for(Profile pp:ps){
				cache.put(pp.name, pp);
			}
		}
		
		Profile p=cache.containsKey(username)?cache.get(username):getProfile(MinecraftUtil.getServerAddress(),username);
		if(p==null)
			return null;
		UserProfile profile=new UserProfile();
		profile.skinUrl=p.skin;
		profile.capeUrl=p.cape;
		if(profile.isEmpty()){
			CustomSkinLoader.logger.info("Profile is empty.");
			return null;
		}
		return profile;
	}
	private boolean isIPChange(String newIP){
		return lastLoginServer==null ? newIP!=null : !lastLoginServer.equalsIgnoreCase(newIP);
	}
	
	public static Profile[] makeLogin(String ip,String username,String elfID){
		String url=LOGIN_URL.replaceAll("%SERVER_IP%", ip).replaceAll("%USERNAME%", username).replaceAll("%ELF_ID%", elfID);
		HttpResponce responce=HttpRequestUtil.makeHttpRequest(new HttpRequest(url));
		
		if(!responce.success||StringUtils.isEmpty(responce.content)){
			CustomSkinLoader.logger.info("Request failed.");
			return null;
		}
		BasicResponce r=CustomSkinLoader.GSON.fromJson(responce.content, BasicResponce.class);
		if(r.error!=0){
			CustomSkinLoader.logger.info("Error "+r.error+": "+r.msg);
			return null;
		}
		if(r.players==null||r.players.length==0){
			CustomSkinLoader.logger.info("No Profile found.");
			return null;
		}
		
		return r.players;
	}
	public static void makeLogout(String ip,String username){
		String url=LOGOUT_URL.replaceAll("%SERVER_IP%", ip).replaceAll("%USERNAME%", username);
		HttpRequestUtil.makeHttpRequest(new HttpRequest(url).setCacheTime(-1));
	}
	public static Profile getProfile(String ip,String username){
		String url=PROFILE_URL.replaceAll("%SERVER_IP%", ip).replaceAll("%USERNAME%", username);
		HttpResponce responce=HttpRequestUtil.makeHttpRequest(new HttpRequest(url));
		
		if(!responce.success||StringUtils.isEmpty(responce.content)){
			CustomSkinLoader.logger.info("Request failed.");
			return null;
		}
		BasicResponce r=CustomSkinLoader.GSON.fromJson(responce.content, BasicResponce.class);
		if(r.error!=0){
			CustomSkinLoader.logger.info("Error "+r.error+": "+r.msg);
			return null;
		}
		if(r.players==null||r.players.length==0){
			CustomSkinLoader.logger.info("Profile not found.");
			return null;
		}
		
		return r.players[0];
	}
	private static class BasicResponce{
		int error;
		String msg;
		
		String gid;
		String expired_at;
		Profile[] players;
	}
	private static class Profile{
		String name;
		String login_time;
		String skin;
		String cape;
	}
	
	
	@Override
	public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
		return true;
	}

	@Override
	public String getName() {
		return "ElfSkin";
	}

	@Override
	public void initLocalFolder(SkinSiteProfile ssp) {
		//No local skin
	}
}
