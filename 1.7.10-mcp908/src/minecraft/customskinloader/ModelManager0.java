package customskinloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

/**
 * Model Manager for 1.7.10 and lower.
 * A manager to check if model is available.
 * It is the only class in package 'customskinloader' which has differences in different Minecraft version.
 * @since 13.1
 */
public class ModelManager0 {
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
	public static UserProfile toUserProfile(Map profile){
		UserProfile userProfile=new UserProfile();
		MinecraftProfileTexture skin=(MinecraftProfileTexture)profile.get(Type.SKIN);
		userProfile.skinUrl= skin==null?null:skin.getUrl();//Avoid NullPointerException
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
	public static Map fromUserProfile(UserProfile profile){
		Map map=Maps.newHashMap();
		if(profile.skinUrl!=null)
			map.put(Type.SKIN, getProfileTexture(profile.skinUrl));
		if(profile.capeUrl!=null)
			map.put(Type.CAPE, getProfileTexture(profile.capeUrl));
		return map;
	}
	
	/**
	 * Parse url to MinecraftProfileTexture
	 * Fix authlib 21 bug
	 * @param url - textureUrl
	 * @return MinecraftProfileTexture
	 * @since 13.2
	 */
	public static MinecraftProfileTexture getProfileTexture(String url){
		Map hashMap=new HashMap<String,String>();
		hashMap.put("url", url);
		Gson gson=new Gson();
		String json=gson.toJson(hashMap);
		MinecraftProfileTexture pt=gson.fromJson(json, MinecraftProfileTexture.class);
		return pt;
	}
	
	private static void refreshModels(){
		models=new ArrayList<String>();
		models.add("default");
	}
}
