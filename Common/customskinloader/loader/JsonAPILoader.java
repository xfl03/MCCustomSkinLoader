package customskinloader.loader;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.mojang.authlib.GameProfile;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.jsonapi.*;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;

public class JsonAPILoader implements ProfileLoader.IProfileLoader {
	public static final String TEXTURES="textures/";
	public static enum Type{
		CustomSkinAPI(new CustomSkinAPI()),UniSkinAPI(new UniSkinAPI());
		public IJsonAPI jsonAPI;
		private Type(IJsonAPI jsonAPI){
			this.jsonAPI=jsonAPI;
		}
	}
	private Type type;
	public JsonAPILoader(Type type){
		this.type=type;
	}
	@Override
	public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile, boolean local) throws Exception {
		String username=gameProfile.getName();
		if(ssp.root==null||ssp.root.equals("")){
			CustomSkinLoader.logger.info("Root not defined.");
			return null;
		}
		String jsonUrl=type.jsonAPI.toJsonUrl(ssp.root, username);
		String json;
		if(local){
			File jsonFile=new File(jsonUrl);
			if(!jsonFile.exists()){
				CustomSkinLoader.logger.info("Profile File not found.");
				return null;
			}
			json=IOUtils.toString(new FileInputStream(jsonFile));
		}else{
			json=HttpUtil0.readHttp(jsonUrl,ssp.userAgent);
		}
		if(json==null||json.equals("")){
			CustomSkinLoader.logger.info("Profile not found.");
			return null;
		}
		UserProfile p=type.jsonAPI.toUserProfile(ssp.root, json, local);
		if(p==null||p.isEmpty()){
			CustomSkinLoader.logger.info("Both skin and cape not found.");
			return null;
		}else{
			return p;
		}
	}
	@Override
	public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
		return ssp0.root.equalsIgnoreCase(ssp1.root);
	}
}
