package customskinloader.loader;

import java.util.HashMap;

import com.mojang.authlib.GameProfile;

import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.UserProfile;

public class ProfileLoader {
	public static final HashMap<String,IProfileLoader> LOADERS=initLoaders();
	
	private static HashMap<String, IProfileLoader> initLoaders() {
		HashMap<String, IProfileLoader> loaders=new HashMap<String, IProfileLoader>();
		loaders.put("mojangapi", new MojangAPILoader());
		loaders.put("customskinapi", new JsonAPILoader(JsonAPILoader.Type.CustomSkinAPI));
		loaders.put("legacy", new LegacyLoader());
		loaders.put("uniskinapi", new JsonAPILoader(JsonAPILoader.Type.UniSkinAPI));
		return loaders;
	}
	
	public interface IProfileLoader {
		public UserProfile loadProfile(SkinSiteProfile ssp,GameProfile gameProfile,boolean local) throws Exception;
		public boolean compare(SkinSiteProfile ssp0,SkinSiteProfile ssp1);
	}
}
