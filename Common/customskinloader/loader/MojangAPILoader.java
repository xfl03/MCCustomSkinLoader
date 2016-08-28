package customskinloader.loader;

import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpUtil0;
import net.minecraft.client.Minecraft;

public class MojangAPILoader implements ProfileLoader.IProfileLoader {

	public static MinecraftSessionService defaultSessionService=null;
	@Override
	public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile, boolean local) throws Exception {
		if(local)
			CustomSkinLoader.logger.warning("!!Local Skin couldn't load by MojangAPI.!!");
		if(defaultSessionService==null){
			CustomSkinLoader.logger.warning("Session Service Not Exist.");
			return null;
		}
		Map<MinecraftProfileTexture.Type,MinecraftProfileTexture> map=defaultSessionService.getTextures(gameProfile, false);
		if(!map.isEmpty()){
			CustomSkinLoader.logger.info("Default profile will be used.");
			return ModelManager0.toUserProfile(map);
		}
		String username=gameProfile.getName();
		GameProfile newGameProfile=loadGameProfile(username);
		if(newGameProfile==null){
			CustomSkinLoader.logger.info("Profile not found.("+username+"'s profile not found.)");
			return null;
		}
		defaultSessionService.fillProfileProperties(newGameProfile, false);
		map=defaultSessionService.getTextures(newGameProfile, false);
		if(!map.isEmpty()){
			gameProfile.getProperties().putAll(newGameProfile.getProperties());
			return ModelManager0.toUserProfile(map);
		}
		CustomSkinLoader.logger.info("Profile not found.("+username+" doesn't have skin/cape.)");
		return null;
	}
	public static GameProfile loadGameProfile(String username) throws JsonSyntaxException{
		//Doc (http://wiki.vg/Mojang_API#Username_-.3E_UUID_at_time)
		String json0=HttpUtil0.readHttp("https://api.mojang.com/users/profiles/minecraft/"+username,null);
		if(json0==null||json0.equals(""))
			return null;
		Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
		GameProfile gameProfile=gson.fromJson(json0, GameProfile.class);
		if(gameProfile.getId()==null)
			return null;
		return new GameProfile(gameProfile.getId(),gameProfile.getName());
	}
	@Override
	public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
		return true;
	}
	@Override
	public String getName() {
		return "MojangAPI";
	}
}
