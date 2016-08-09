package customskinloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import customskinloader.config.Config;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.*;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.ProfileCache;
import customskinloader.profile.UserProfile;
import customskinloader.utils.MinecraftUtil;

/**
 * Custom skin loader mod for Minecraft.
 * @author (C) Jeremy Lam [JLChnToZ] 2013 & Alexander Xia [xfl03] 2014-2016
 * @version 14.3 (2016.8.9)
 */
public class CustomSkinLoader {
	public static final String CustomSkinLoader_VERSION="14.3";
	public static final File DATA_DIR=new File(MinecraftUtil.getMinecraftDataDir0(),"CustomSkinLoader"),
			LOG_FILE=new File(DATA_DIR,"CustomSkinLoader.log"),
			CONFIG_FILE=new File(DATA_DIR,"CustomSkinLoader.json");
	public static final SkinSiteProfile[] DEFAULT_LOAD_LIST={
			SkinSiteProfile.createMojangAPI("Mojang"),
			SkinSiteProfile.createCustomSkinAPI("BlessingSkin","https://skin.prinzeugen.net/csl/"),
			SkinSiteProfile.createCustomSkinAPI("OneSkin","http://fleey.org/skin/skin_user/skin_json.php/"),
			//Minecrack could not load skin correctly
			//SkinSiteProfile.creatLegacy("Minecrack","http://minecrack.fr.nf/mc/skinsminecrackd/{USERNAME}.png","http://minecrack.fr.nf/mc/cloaksminecrackd/{USERNAME}.png"),
			SkinSiteProfile.createUniSkinAPI("SkinMe","http://www.skinme.cc/uniskin/"),
			SkinSiteProfile.createCustomSkinAPI("McSkin","http://www.mcskin.cc/"),
			SkinSiteProfile.createLegacy("LocalSkin", true, "LocalSkin/skins/{USERNAME}.png", "LocalSkin/capes/{USERNAME}.png")};
	public static final HashMap<String,IProfileLoader> LOADERS=initLoaders();
	
	public static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
	public static final Logger logger=initLogger();
	public static final Config config=loadConfig0();
	
	private static final ProfileCache profileCache=new ProfileCache();
	
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
		for(int i=0;i<config.loadlist.length;i++){
			SkinSiteProfile ssp=config.loadlist[i];
			logger.info((i+1)+"/"+config.loadlist.length+" Try to load profile from '"+ssp.name+"'.");
			IProfileLoader loader=LOADERS.get(ssp.type.toLowerCase());
			if(loader==null){
				logger.info("Type '"+ssp.type+"' is not defined.");
				continue;
			}
			UserProfile profile=null;
			try{
				profile=loader.loadProfile(ssp, gameProfile,(ssp.local==null?false:ssp.local==true));
			}catch(Exception e){
				logger.warning("Exception occurs while loading.");
				logger.warning(e);
			}
			if(profile==null)
				continue;
			logger.info(username+"'s profile loaded.");
			profileCache.updateCache(username, profile);
			profileCache.setLoading(username, false);
			logger.info(profile.toString(profileCache.getExpiry(username)));
			return profile;
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
		if(username==null){
			logger.warning("Could not load profile from cache: username is null.");
			return Maps.newHashMap();
		}
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
		logger.info("CustomSkinLoader "+CustomSkinLoader_VERSION);
		logger.info("DataDir: "+DATA_DIR.getAbsolutePath());
		logger.info("Minecraft: "+MinecraftUtil.getMinecraftVersion());
		return logger;
	}

	private static HashMap<String, IProfileLoader> initLoaders() {
		HashMap<String, IProfileLoader> loaders=new HashMap<String, IProfileLoader>();
		loaders.put("mojangapi", new MojangAPILoader());
		loaders.put("customskinapi", new JsonAPILoader(JsonAPILoader.Type.CustomSkinAPI));
		loaders.put("legacy", new LegacyLoader());
		loaders.put("uniskinapi", new JsonAPILoader(JsonAPILoader.Type.UniSkinAPI));
		return loaders;
	}

	private static Config loadConfig0() {
		Config config=loadConfig();
		logger.info("Enable:"+config.enable+
				", EnableSkull:"+config.enableSkull+
				", EnableTranSkin:"+config.enableTransparentSkin+
				", CacheExpiry:"+config.cacheExpiry+
				", enableUpdateSkull:"+config.enableUpdateSkull+
				", LocalProfileCache:"+config.enableLocalProfileCache+
				", LoadList:"+(config.loadlist==null?0:config.loadlist.length));
		if(config.version==null||Float.parseFloat(CustomSkinLoader_VERSION)-Float.parseFloat(config.version)>0.01){
			logger.info("Config File is out of date: "+config.version);
			config.version=CustomSkinLoader_VERSION;
			writeConfig(config,true);
		}
		return config;
	}

	private static Config loadConfig() {
		logger.info("Config File: "+CONFIG_FILE.getAbsolutePath());
		if(!CONFIG_FILE.exists()){
			logger.info("Config file not found, use default instead.");
			return initConfig();
		}
		try {
			logger.info("Try to load config.");
			String json=IOUtils.toString(new FileInputStream(CONFIG_FILE));
			Config config=GSON.fromJson(json, Config.class);
			logger.info("Successfully load config.");
			return config;
		}catch (Exception e) {
			logger.info("Failed to load config, use default instead.("+e.toString()+")");
			File brokenFile=new File(DATA_DIR,"BROKEN-CustomSkinLoader.json");
			if(brokenFile.exists())
				brokenFile.delete();
			CONFIG_FILE.renameTo(brokenFile);
			return initConfig();
		}
	}

	private static Config initConfig() {
		Config config=new Config(DEFAULT_LOAD_LIST);
		writeConfig(config,false);
		return config;
	}
	private static void writeConfig(Config config,boolean update){
		String json=GSON.toJson(config);
		if(CONFIG_FILE.exists())
			CONFIG_FILE.delete();
		try {
			CONFIG_FILE.createNewFile();
			IOUtils.write(json, new FileOutputStream(CONFIG_FILE));
			logger.info("Successfully "+(update?"update":"create")+" config.");
		} catch (Exception e) {
			logger.info("Failed to "+(update?"update":"create")+" config.("+e.toString()+")");
		}
	}
}
