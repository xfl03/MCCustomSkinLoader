package customskinloader.loader.jsonapi;

import java.util.List;
import java.util.Map;

import customskinloader.CustomSkinLoader;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpTextureUtil;

public class UniSkinAPI implements IJsonAPI {
	private static final String TEXTURES="textures/";
	private static final String SUFFIX=".json";

	@Override
	public String toJsonUrl(String root, String username) {
		return new StringBuilder().append(root).append(username).append(SUFFIX).toString();
	}

	@Override
	public UserProfile toUserProfile(String root, String json, boolean local) {
		UniSkinAPIProfile profile=CustomSkinLoader.GSON.fromJson(json, UniSkinAPIProfile.class);
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
	/**
	 * Json profile for UniSkinAPI
	 * Source Code: https://github.com/RecursiveG/UniSkinMod/blob/master/src/main/java/org/devinprogress/uniskinmod/UniSkinApiProfile.java#L18-L22
	 * @author RecursiveG
	 */
	private class UniSkinAPIProfile{
		public String player_name;
        public long last_update;
        public List<String> model_preference;
        public Map<String,String> skins;
        public String cape;
        
        public int errno;
        public String msg;
	}

}
