package customskinloader.loader;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;

import customskinloader.CustomSkinLoader;
import customskinloader.ModelManager;
import customskinloader.UserProfile;
import customskinloader.config.SkinSiteProfile;
import customskinloader.utils.HttpUtil;

public class MojangAPILoader implements IProfileLoader {

	@Override
	public UserProfile loadProfile(SkinSiteProfile ssp, String username) throws Exception {
		//Get UUID Begin (http://wiki.vg/Mojang_API#Username_-.3E_UUID_at_time)
		String json0=HttpUtil.readHttp("https://api.mojang.com/users/profiles/minecraft/"+username);
		if(json0==null||json0.equals("")){
			CustomSkinLoader.logger.info("Profile not found.("+username+"'s UUID not found.)");
			return null;
		}
		Gson gson=new Gson();
		UUIDProfile uuidProfile=gson.fromJson(json0, UUIDProfile.class);
		if(uuidProfile.id==null||uuidProfile.id.equals("")){
			CustomSkinLoader.logger.info("Profile not found.("+username+"'s UUID is empty.)");
			return null;
		}
		//Get UUID End
		//Get Profile Begin (http://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape)
		String json1=HttpUtil.readHttp("https://sessionserver.mojang.com/session/minecraft/profile/"+uuidProfile.id);
		if(json1==null||json1.equals("")){
			CustomSkinLoader.logger.info("Profile not found.("+username+"'s Profile is empty.)");
			return null;
		}
		UserProfile0 profile= gson.fromJson(json1, UserProfile0.class);
		//Get Profile End
		//Get Texture Begin
		Property p=profile.properties.length>=1?profile.properties[0]:null;
		if(p==null){
			CustomSkinLoader.logger.info("Profile not found.("+username+"'s Profile doesn't have property for texture.)");
			return null;
		}
		String texture=p.getValue();
		if(texture==null||texture.equals("")){
			CustomSkinLoader.logger.info("Profile not found.("+username+"'s texture not found.)");
			return null;
		}
		String json2=new String(Base64.decodeBase64(texture));
		UserTexture texture1=gson.fromJson(json2, UserTexture.class);
		if(texture1.textures==null||texture1.textures.isEmpty()){
			CustomSkinLoader.logger.info("Profile not found.("+username+" doesn't have skin/cape.)");
			return null;
		}
		//Get Texture End
		return ModelManager.toUserProfile(texture1.textures);
	}
	
	private class UUIDProfile{
		public String id;
		public String name;
	}
	
	private class UserProfile0{
		public String id;
		public String name;
		public Property[] properties;
	}
	private class UserTexture{
		public HashMap<Type,MinecraftProfileTexture> textures;
	}
}
