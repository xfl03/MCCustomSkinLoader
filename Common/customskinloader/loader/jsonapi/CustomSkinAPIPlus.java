package customskinloader.loader.jsonapi;

import com.google.gson.Gson;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.JsonAPILoader.IJsonAPI;
import customskinloader.profile.UserProfile;
import customskinloader.utils.MinecraftUtil;

public class CustomSkinAPIPlus implements IJsonAPI {

	@Override
	public String toJsonUrl(String root, String username) {
		return JsonAPILoader.Type.CustomSkinAPI.jsonAPI.toJsonUrl(root, username);
	}

	@Override
	public String getPayload(SkinSiteProfile ssp) {
		return new Gson().toJson(new CustomSkinAPIPlusPayload());
	}

	@Override
	public UserProfile toUserProfile(String root, String json, boolean local) {
		return JsonAPILoader.Type.CustomSkinAPI.jsonAPI.toUserProfile(root, json, local);
	}

	@Override
	public String getName() {
		return "CustomSKinAPIPlus";
	}

	public static class CustomSkinAPIPlusPayload{
		public static String gameVersion = MinecraftUtil.getMinecraftMainVersion();//minecraft version
		public static String modVersion = CustomSkinLoader.CustomSkinLoader_FULL_VERSION;//mod version
		public String serverAddress = MinecraftUtil.isLanServer()?null:MinecraftUtil.getServerAddress();//ip:port
	}
	public static class CustomSkinAPIPlusPrivacy{
		public static boolean gameVersion;
		public static boolean modVersion;
		public static boolean serverAddress;
	}
}
