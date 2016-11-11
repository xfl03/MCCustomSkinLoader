package customskinloader.loader;

import java.util.HashMap;

import com.mojang.authlib.GameProfile;

import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.UserProfile;

public class ProfileLoader {
	private static final IProfileLoader[] DEFAULT_LOADERS={
			new MojangAPILoader(),
			new JsonAPILoader(JsonAPILoader.Type.CustomSkinAPI),
			new JsonAPILoader(JsonAPILoader.Type.CustomSkinAPIPlus),
			new LegacyLoader(),
			new JsonAPILoader(JsonAPILoader.Type.UniSkinAPI),
			new ElfSkinLoader()};
	
	public static final HashMap<String,IProfileLoader> LOADERS=initLoaders();
	
	private static HashMap<String, IProfileLoader> initLoaders() {
		HashMap<String, IProfileLoader> loaders=new HashMap<String, IProfileLoader>();
		for(IProfileLoader loader:DEFAULT_LOADERS){
			loaders.put(loader.getName().toLowerCase(), loader);
		}
		return loaders;
	}
	
	public interface IProfileLoader {
		public UserProfile loadProfile(SkinSiteProfile ssp,GameProfile gameProfile) throws Exception;
		public boolean compare(SkinSiteProfile ssp0,SkinSiteProfile ssp1);
		public String getName();
		public void initLocalFolder(SkinSiteProfile ssp);
	}
}
