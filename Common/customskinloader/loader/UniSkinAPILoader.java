package customskinloader.loader;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;

public class UniSkinAPILoader implements IProfileLoader {
	public static final String TEXTURES="textures/";
	@Override
	public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
		String username=gameProfile.getName();
		if(ssp.root==null||ssp.root.equals("")){
			CustomSkinLoader.logger.info("Root not defined.");
			return null;
		}
		String json=HttpUtil0.readHttp(ssp.root+username+".json",ssp.userAgent);
		if(json==null||json.equals("")){
			CustomSkinLoader.logger.info("Profile not found.");
			return null;
		}
		UserProfile p=toUserProfile(ssp.root,json,false);
		if(p==null||p.isEmpty()){
			CustomSkinLoader.logger.info("Both skin and cape not found.");
			return null;
		}else{
			return p;
		}
	}
	/**
	 * Json profile for UniSkinAPI
	 * Source Code: https://github.com/RecursiveG/UniSkinMod/blob/master/src/main/java/org/devinprogress/uniskinmod/UniSkinApiProfile.java#L18-L22
	 * @author RecursiveG
	 */
	private class UniSkinAPI{
		public String player_name;
        public long last_update;
        public List<String> model_preference;
        public Map<String,String> skins;
        public String cape;
        
        public int errno;
        public String msg;
	}
	@Override
	public UserProfile loadLocalProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
		String username=gameProfile.getName();
		if(ssp.root==null||ssp.root.equals("")){
			CustomSkinLoader.logger.info("Root not defined.");
			return null;
		}
		File jsonFile=new File(CustomSkinLoader.DATA_DIR,ssp.root+username+".json");
		if(!jsonFile.exists()){
			CustomSkinLoader.logger.info("Profile File not found.");
			return null;
		}
		String json=IOUtils.toString(new FileInputStream(jsonFile));
		if(json==null||json.equals("")){
			CustomSkinLoader.logger.info("Profile not found.");
			return null;
		}
		UserProfile p=toUserProfile(ssp.root,json,true);
		if(p==null||p.isEmpty()){
			CustomSkinLoader.logger.info("Both skin and cape not found.");
			return null;
		}else{
			return p;
		}
	}
	
	private UserProfile toUserProfile(String root,String json,boolean local){
		UniSkinAPI profile=CustomSkinLoader.GSON.fromJson(json, UniSkinAPI.class);
		if(profile.errno!=0){
			CustomSkinLoader.logger.info("Error: "+profile.msg);
			return null;
		}
		UserProfile p=new UserProfile();
		if(profile.skins!=null && !profile.skins.isEmpty()){
			if(profile.model_preference!=null && !profile.model_preference.isEmpty()){
				for(String s:profile.model_preference){
					if(ModelManager0.checkModel(s)){
						if(profile.skins.get(s)==null||profile.skins.get(s).equals(""))
							continue;
						p.skinUrl=root+TEXTURES+profile.skins.get(s);
						if(local)
							p.skinUrl=HttpTextureUtil.getLocalFakeUrl(p.skinUrl);
						p.model=s;
						break;
					}
				}		
			}
			if(p.skinUrl==null){
				String s=profile.skins.get("default");
				if(s!=null){
					p.skinUrl=root+TEXTURES+s;
					if(local)
						p.skinUrl=HttpTextureUtil.getLocalFakeUrl(p.skinUrl);
					p.model="default";
				}
			}
		}
		if(profile.skins.get("cape")!=null && !profile.skins.get("cape").equals("")){
			p.capeUrl=root+TEXTURES+profile.skins.get("cape");
			if(local)
				p.capeUrl=HttpTextureUtil.getLocalFakeUrl(p.capeUrl);
		}else if(profile.cape!=null && !profile.cape.equals("")){
			p.capeUrl=root+TEXTURES+profile.cape;
			if(local)
				p.capeUrl=HttpTextureUtil.getLocalFakeUrl(p.capeUrl);
		}
		return p;
	}
}
