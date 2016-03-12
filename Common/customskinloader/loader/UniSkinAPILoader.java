package customskinloader.loader;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import customskinloader.CustomSkinLoader;
import customskinloader.ModelManager0;
import customskinloader.UserProfile;
import customskinloader.config.SkinSiteProfile;
import customskinloader.utils.HttpUtil0;

public class UniSkinAPILoader implements IProfileLoader {
	public static final String TEXTURES="textures/";
	@Override
	public UserProfile loadProfile(SkinSiteProfile ssp, String username) throws Exception {
		if(ssp.root==null||ssp.root.equals("")){
			CustomSkinLoader.logger.info("Root not denined.");
			return null;
		}
		String json=HttpUtil0.readHttp(ssp.root+username+".json");
		if(json==null||json.equals("")){
			CustomSkinLoader.logger.info("Profile not found.");
			return null;
		}
		UniSkinAPI profile=new Gson().fromJson(json, UniSkinAPI.class);
		UserProfile p=new UserProfile();
		if(profile.skins!=null && !profile.skins.isEmpty()){
			if(profile.model_preference!=null && !profile.model_preference.isEmpty()){
				for(String s:profile.model_preference){
					if(ModelManager0.checkModel(s)){
						if(profile.skins.get(s)==null||profile.skins.get(s).equals(""))
							continue;
						p.skinUrl=ssp.root+TEXTURES+profile.skins.get(s);
						p.model=s;
						break;
					}
				}		
			}
			if(p.skinUrl==null){
				String s=profile.skins.get("default");
				if(s!=null){
					p.skinUrl=ssp.root+TEXTURES+s;
					p.model="default";
				}
			}
		}
		if(profile.skins.get("cape")!=null && !profile.skins.get("cape").equals("")){
			p.capeUrl=ssp.root+TEXTURES+profile.skins.get("cape");
		}else if(profile.cape!=null && !profile.cape.equals("")){
			p.capeUrl=ssp.root+TEXTURES+profile.cape;
		}
		if(p.isEmpty()){
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
	}
}
