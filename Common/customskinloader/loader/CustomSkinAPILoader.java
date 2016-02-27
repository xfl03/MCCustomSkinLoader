package customskinloader.loader;

import java.util.HashMap;
import java.util.Set;

import com.google.gson.Gson;

import customskinloader.CustomSkinLoader;
import customskinloader.ModelManager;
import customskinloader.UserProfile;
import customskinloader.config.SkinSiteProfile;
import customskinloader.utils.HttpUtil;

public class CustomSkinAPILoader implements IProfileLoader {
	public static final String TEXTURES="textures/";
	@Override
	public UserProfile loadProfile(SkinSiteProfile ssp, String username) throws Exception {
		if(ssp.root==null||ssp.root.equals("")){
			CustomSkinLoader.logger.info("Root not denined.");
			return null;
		}
		String json=HttpUtil.readHttp(ssp.root+username+".json");
		if(json==null||json.equals("")){
			CustomSkinLoader.logger.info("Profile not found.");
			return null;
		}
		CustomSkinAPI profile=new Gson().fromJson(json, CustomSkinAPI.class);
		UserProfile p=new UserProfile();
		if(profile.skins!=null && !profile.skins.isEmpty()){
			Set<String> keys=profile.skins.keySet();
			for(String s:keys){
				if(ModelManager.checkModel(s)){
					p.skinUrl=ssp.root+TEXTURES+profile.skins.get(s);
					p.model=s;
					break;
				}
			}
			if(p.skinUrl==null){
				p.skinUrl=ssp.root+TEXTURES+profile.skins.get(keys.toArray()[0]);
				p.model="default";
			}
		}else if(profile.skin!=null && !profile.skin.equals("")){
			p.skinUrl=ssp.root+TEXTURES+profile.skin;
		}
		if(profile.cape!=null && !profile.cape.equals("")){
			p.capeUrl=ssp.root+TEXTURES+profile.cape;
		}
		if(p.isEmpty()){
			CustomSkinLoader.logger.info("Both skin and cape not found.");
			return null;
		}else{
			return p;
		}
	}

	private class CustomSkinAPI{
		public String username;
		public HashMap<String,String> skins;
		public String skin;
		public String cape;
	}
}
