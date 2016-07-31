package customskinloader.loader;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;

public class CustomSkinAPILoader implements IProfileLoader {
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
		if(p.isEmpty()){
			CustomSkinLoader.logger.info("Both skin and cape not found.");
			return null;
		}else{
			return p;
		}
	}

	private class CustomSkinAPI{
		public String username;
		public Map<String,String> skins;
		public String skin;
		public String cape;
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
		if(p.isEmpty()){
			CustomSkinLoader.logger.info("Both skin and cape not found.");
			return null;
		}else{
			return p;
		}
	}
	
	private UserProfile toUserProfile(String root,String json,boolean local){
		CustomSkinAPI profile=CustomSkinLoader.GSON.fromJson(json, CustomSkinAPI.class);
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
}
