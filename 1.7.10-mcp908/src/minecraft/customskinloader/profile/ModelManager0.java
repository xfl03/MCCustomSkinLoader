package customskinloader.profile;

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
	public static enum Model{
		SKIN_DEFAULT,
		SKIN_SLIM,
		CAPE,
		ELYTRA
	}
	private static HashMap<String,Model> models=new HashMap<String,Model>();
	private static Type typeElytra=null;
	static{
		models.put("default", Model.SKIN_DEFAULT);
		models.put("cape", Model.CAPE);
	}
	
	/**
	 * Get enum for the model.
	 * @param model - string model
	 * @since 14.5
	 */
	public static Model getEnumModel(String model){
		return models.get(model);
	}
	
	/**
	 * Check if elytra is supported.
	 * @since 14.5
	 */
	public static boolean isElytraSupported(){
		return false;
	}
	
	/**
	 * Parse hashMapProfile to UserProfile
	 * @param profile - hashMapProfile (HashMap<String,MinecraftProfileTexture>)
	 * @return profile - UserProfile instance
	 * @since 13.1
	 */
	public static UserProfile toUserProfile(Map profile){
		UserProfile userProfile=new UserProfile();
		if(profile==null)
			return userProfile;
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
		if(profile==null)
			return map;
		if(profile.skinUrl!=null)
			map.put(Type.SKIN, getProfileTexture(profile.skinUrl,null));
		if(profile.capeUrl!=null)
			map.put(Type.CAPE, getProfileTexture(profile.capeUrl,null));
		return map;
	}
	
	/**
	 * Parse url to MinecraftProfileTexture
	 * Fix authlib 21 bug for 1.7.10
	 * @param url - textureUrl
	 *        metadata - metadata
	 * @return MinecraftProfileTexture
	 * @since 14.5
	 */
	public static MinecraftProfileTexture getProfileTexture(String url,Map metadata){
		Map hashMap=new HashMap<String,String>();
		hashMap.put("url", url);
		Gson gson=new Gson();
		String json=gson.toJson(hashMap);
		MinecraftProfileTexture pt=gson.fromJson(json, MinecraftProfileTexture.class);
		return pt;
	}
}
