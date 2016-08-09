package customskinloader.loader.jsonapi;

import java.util.Map;
import java.util.Set;

import customskinloader.CustomSkinLoader;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpTextureUtil;

public class CustomSkinAPI implements IJsonAPI {
	private static final String TEXTURES="textures/";
	private static final String SUFFIX=".json";

	@Override
	public String toJsonUrl(String root, String username) {
		return new StringBuilder().append(root).append(username).append(SUFFIX).toString();
	}

	@Override
	public UserProfile toUserProfile(String root, String json, boolean local) {
		CustomSkinAPIProfile profile=CustomSkinLoader.GSON.fromJson(json, CustomSkinAPIProfile.class);
		UserProfile p=new UserProfile();
		if(profile.skins!=null && !profile.skins.isEmpty()){
			Set<String> keys=profile.skins.keySet();
			for(String s:keys){
				//System.out.println(s+" "+ModelManager0.checkModel(s));
				if(ModelManager0.checkModel(s)){
					if(profile.skins.get(s)==null||profile.skins.get(s).equals(""))
							continue;
					p.skinUrl=root+TEXTURES+profile.skins.get(s);
					p.model=s;
					break;
				}
			}
			if(p.skinUrl==null){
				p.skinUrl=root+TEXTURES+profile.skins.get(keys.toArray()[0]);
				p.model="default";
			}
		}else if(profile.skin!=null && !profile.skin.equals("")){
			p.skinUrl=root+TEXTURES+profile.skin;
			if(local)
				p.skinUrl=HttpTextureUtil.getLocalFakeUrl(p.skinUrl);
		}
		if(profile.cape!=null && !profile.cape.equals("")){
			p.capeUrl=root+TEXTURES+profile.cape;
			if(local)
				p.capeUrl=HttpTextureUtil.getLocalFakeUrl(p.capeUrl);
		}
		return p;
	}
	private class CustomSkinAPIProfile{
		public String username;
		public Map<String,String> skins;
		public String skin;
		public String cape;
	}

}
