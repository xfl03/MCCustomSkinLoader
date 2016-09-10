package customskinloader.profile;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

/**
 * Model Manager for 1.8 and higher.
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
		if(profile==null)
			return userProfile;
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
	public static Map fromUserProfile(UserProfile profile){
		Map map=Maps.newHashMap();
		if(profile==null)
			return map;
		if(profile.skinUrl!=null){
			Map metadata=null;
			if(profile.model!=null&&profile.model.equals("slim")){
				metadata = Maps.newHashMap();
				metadata.put("model", "slim");
			}
			map.put(Type.SKIN, getProfileTexture(profile.skinUrl,metadata));
		}
		if(profile.capeUrl!=null)
			map.put(Type.CAPE, getProfileTexture(profile.capeUrl,null));
		return map;
	}
	
	/**
	 * Parse url to MinecraftProfileTexture
	 * 
	 * @param url - textureUrl
	 *        metadata - metadata
	 * @return MinecraftProfileTexture
	 * @since 14.5
	 */
	public static MinecraftProfileTexture getProfileTexture(String url,Map metadata){
		return new MinecraftProfileTexture(url,metadata);
	}
	
	private static void refreshModels(){
		models=new ArrayList<String>();
		models.add("default");
		models.add("slim");
	}
}
