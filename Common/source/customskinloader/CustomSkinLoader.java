package customskinloader;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import customskinloader.config.Config;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.*;
import customskinloader.profile.DynamicSkullManager;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.ProfileCache;
import customskinloader.profile.UserProfile;
import customskinloader.utils.MinecraftUtil;

/**
 * Custom skin loader mod for Minecraft.
 * @author (C) Jeremy Lam [JLChnToZ] 2013 & Alexander Xia [xfl03] 2014-2016
 * @version @MOD_FULL_VERSION@
 */
public class CustomSkinLoader {
	public static final String CustomSkinLoader_VERSION="@MOD_VERSION@";
	public static final String CustomSkinLoader_FULL_VERSION="@MOD_FULL_VERSION@";
	
	public static final File DATA_DIR=new File(MinecraftUtil.getMinecraftDataDir(),"CustomSkinLoader"),
			LOG_FILE=new File(DATA_DIR,"CustomSkinLoader.log"),
			CONFIG_FILE=new File(DATA_DIR,"CustomSkinLoader.json");
	public static final SkinSiteProfile[] DEFAULT_LOAD_LIST={
			SkinSiteProfile.createMojangAPI("Mojang"),
			SkinSiteProfile.createCustomSkinAPI("BlessingSkin","http://123.57.132.134/"),
			SkinSiteProfile.createCustomSkinAPI("OneSkin","http://fleey.cn/skin/skin_user/skin_json.php/"),
			//Minecrack could not load skin correctly
			//SkinSiteProfile.creatLegacy("Minecrack","http://minecrack.fr.nf/mc/skinsminecrackd/{USERNAME}.png","http://minecrack.fr.nf/mc/cloaksminecrackd/{USERNAME}.png",null),
			SkinSiteProfile.createUniSkinAPI("SkinMe","http://www.skinme.cc/uniskin/"),
			SkinSiteProfile.createLegacy("LocalSkin","LocalSkin/skins/{USERNAME}.png","LocalSkin/capes/{USERNAME}.png","LocalSkin/elytras/{USERNAME}.png")};
	
	public static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
	public static final Logger logger=initLogger();
	public static final Config config=Config.loadConfig0();
	
	private static final ProfileCache profileCache=new ProfileCache();
	private static final DynamicSkullManager dynamicSkullManager=new DynamicSkullManager();
	
	//For User Skin
	public static Map loadProfile(GameProfile gameProfile){
		String username=gameProfile.getName();
		//Fix: http://hopper.minecraft.net/crashes/minecraft/MCX-2773713
		if(username==null){
			logger.warning("Could not load profile: username is null.");
			return Maps.newHashMap();
		}
		String tempName=Thread.currentThread().getName();
		Thread.currentThread().setName(username);//Change Thread Name
		UserProfile profile=null;
		if(profileCache.isReady(username)){
			logger.info("Cached profile will be used.");
			profile=profileCache.getProfile(username);
			if(profile==null){
				logger.warning("(!Cached Profile is empty!) Expiry:"+profileCache.getExpiry(username));
				if(profileCache.isExpired(username))//force load
					profile=loadProfile0(gameProfile);
			}
			else
				logger.info(profile.toString(profileCache.getExpiry(username)));
		}else{
			profileCache.setLoading(username, true);
			profile=loadProfile0(gameProfile);
		}
		Thread.currentThread().setName(tempName);
		return ModelManager0.fromUserProfile(profile);
	}
	//Core
	public static UserProfile loadProfile0(GameProfile gameProfile){
		String username=gameProfile.getName();
		profileCache.setLoading(username, true);
		logger.info("Loading "+username+"'s profile.");
		if(config.loadlist==null||config.loadlist.isEmpty()){
			logger.info("LoadList is Empty.");
			return null;
		}
		
		UserProfile profile0=new UserProfile();
		for(int i=0;i<config.loadlist.size();i++){
			SkinSiteProfile ssp=config.loadlist.get(i);
			logger.info((i+1)+"/"+config.loadlist.size()+" Try to load profile from '"+ssp.name+"'.");
			ProfileLoader.IProfileLoader loader=ProfileLoader.LOADERS.get(ssp.type.toLowerCase());
			if(loader==null){
				logger.info("Type '"+ssp.type+"' is not defined.");
				continue;
			}
			UserProfile profile=null;
			try{
				profile=loader.loadProfile(ssp, gameProfile);
			}catch(Exception e){
				logger.warning("Exception occurs while loading.");
				logger.warning(e);
			}
			if(profile==null)
				continue;
			if(!config.forceLoadAllTextures){
				profile0=profile;
				break;
			}
			profile0.mix(profile);
			if(profile0.isFull())
				break;
		}
		if(!profile0.isEmpty()){
			logger.info(username+"'s profile loaded.");
			profileCache.updateCache(username, profile0);
			profileCache.setLoading(username, false);
			logger.info(profile0.toString(profileCache.getExpiry(username)));
			return profile0;
		}
		logger.info(username+"'s profile not found in load list.");
		
		if(config.enableLocalProfileCache){
			UserProfile profile=profileCache.getLocalProfile(username);
			if(profile==null)
				logger.info(username+"'s LocalProfile not found.");
			else{
				profileCache.updateCache(username, profile, false);
				profileCache.setLoading(username, false);
				logger.info(username+"'s LocalProfile will be used.");
				logger.info(profile.toString(profileCache.getExpiry(username)));
				return profile;
			}
		}
		profileCache.setLoading(username, false);
		return null;
	}
	
	//For Skull
	public static Map<Type, MinecraftProfileTexture> loadProfileFromCache(final GameProfile gameProfile) {
		String username=gameProfile.getName();
		if(username==null)
			return dynamicSkullManager.getTexture(gameProfile);
		if(config.enableUpdateSkull?profileCache.isReady(username):profileCache.isExist(username)){
			UserProfile profile=profileCache.getProfile(username);
			return ModelManager0.fromUserProfile(profile);
		}
		//profileCache.setLoading(username, true);
		Thread loadThread=new Thread(){
			public void run(){
				loadProfile0(gameProfile);//Load in thread
			}
		};
		loadThread.setName(username+"'s skull");
		loadThread.start();
		return Maps.newHashMap();
	}
	
	private static Logger initLogger() {
		Logger logger=new Logger(LOG_FILE);
		logger.info("CustomSkinLoader "+CustomSkinLoader_FULL_VERSION);
		logger.info("DataDir: "+DATA_DIR.getAbsolutePath());
		logger.info("Minecraft: "+MinecraftUtil.getMinecraftMainVersion()+"("+MinecraftUtil.getMinecraftVersionText()+")");
		return logger;
	}
}
