package customskinloader;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

/**
 * Model Manager for 1.8 and higher.
 * A manager to check if model is available.
 * It is the only class in package 'customskinloader' which has differences in different Minecraft version.
 * @since 13.1
 */
public class ModelManager {
	private static ArrayList<String> models=null;
	
	/**
	 * Check if model is available.
	 * @param model - default/slim
	 * @since 13.1
	 */
	public static boolean checkModel(String model){
		if(models==null)
			refreshModels();
		return models.contains(model);
	}
	
	/**
	 * Parse hashMapProfile to UserProfile
	 * @param profile - hashMapProfile (HashMap<String,MinecraftProfileTexture>)
	 * @return profile - UserProfile instance
	 * @since 13.1
	 */
	public static UserProfile toUserProfile(HashMap profile){
		UserProfile userProfile=new UserProfile();
		MinecraftProfileTexture skin=(MinecraftProfileTexture)profile.get(Type.SKIN);
		userProfile.skinUrl= skin==null?null:skin.getUrl();//Avoid NullPointerException
		userProfile.model= skin==null?null:skin.getMetadata("model");
		if(userProfile.model==null||userProfile.model.equals(""))
			userProfile.model="default";
		MinecraftProfileTexture cape=(MinecraftProfileTexture)profile.get(Type.CAPE);
		userProfile.capeUrl= cape==null?null:cape.getUrl();
		return userProfile;
	}
	
	/**
	 * Parse UserProfile to hashMapProfile
	 * @param profile - UserProfile instance
	 * @return profile - hashMapProfile (HashMap<String,MinecraftProfileTexture>)
	 * @since 13.1
	 */
	public static HashMap fromUserProfile(UserProfile profile){
		HashMap hashMap=Maps.newHashMap();
		if(profile.skinUrl!=null){
			HashMap metadata=null;
			if(profile.model!=null&&profile.model.equals("slim")){
				metadata = Maps.newHashMap();
				metadata.put("model", "slim");
			}
			hashMap.put(Type.SKIN, new MinecraftProfileTexture(profile.skinUrl,metadata));
		}
		if(profile.capeUrl!=null)
			hashMap.put(Type.CAPE, new MinecraftProfileTexture(profile.capeUrl,null));
		return hashMap;
	}
	
	private static void refreshModels(){
		models=new ArrayList<String>();
		models.add("default");
		models.add("slim");
	}
}
