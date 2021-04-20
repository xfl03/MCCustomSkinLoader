package customskinloader;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.config.Config;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.ProfileLoader;
import customskinloader.profile.DynamicSkullManager;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.ProfileCache;
import customskinloader.profile.UserProfile;
import customskinloader.utils.MinecraftUtil;

/**
 * Custom skin loader mod for Minecraft.
 * @author (C) Jeremy Lam [JLChnToZ] 2013-2014 & Alexander Xia [xfl03] 2014-2020
 * @version @MOD_FULL_VERSION@
 */
public class CustomSkinLoader {
    public static final String CustomSkinLoader_VERSION="@MOD_VERSION@";
    public static final String CustomSkinLoader_FULL_VERSION="@MOD_FULL_VERSION@";
    public static final int CustomSkinLoader_BUILD_NUMBER=Integer.parseInt("@MOD_BUILD_NUMBER@");
    
    public static final File DATA_DIR=new File(MinecraftUtil.getMinecraftDataDir(),"CustomSkinLoader"),
            LOG_FILE=new File(DATA_DIR,"CustomSkinLoader.log"),
            CONFIG_FILE=new File(DATA_DIR,"CustomSkinLoader.json");
    
    public static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    public static final Logger logger=initLogger();
    public static final Config config=Config.loadConfig0();
    
    private static final ProfileCache profileCache=new ProfileCache();
    private static final DynamicSkullManager dynamicSkullManager=new DynamicSkullManager();

    //Correct thread name in thread pool
    private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
    private static final ThreadFactory customFactory = r -> {
        Thread t = defaultFactory.newThread(r);
        if(r instanceof Thread) {
            t.setName(((Thread) r).getName());
        }
        return t;
    };
    //Thread pool will discard oldest task when queue reaches 333 tasks
    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            config.threadPoolSize, config.threadPoolSize, 1L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(333), customFactory, new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    //For User Skin
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadProfile(GameProfile gameProfile){
        String username=gameProfile.getName();
        String credential=MinecraftUtil.getCredential(gameProfile);
        //Fix: http://hopper.minecraft.net/crashes/minecraft/MCX-2773713
        if(username==null){
            logger.warning("Could not load profile: username is null.");
            return Maps.newHashMap();
        }
        String tempName=Thread.currentThread().getName();
        Thread.currentThread().setName(username);//Change Thread Name
        UserProfile profile;
        if(profileCache.isReady(credential)){
            logger.info("Cached profile will be used.");
            profile=profileCache.getProfile(credential);
            if(profile==null){
                logger.warning("(!Cached Profile is empty!) Expiry:"+profileCache.getExpiry(credential));
                if(profileCache.isExpired(credential))//force load
                    profile=loadProfile0(gameProfile);
            }
            else
                logger.info(profile.toString(profileCache.getExpiry(credential)));
        }else{
            profileCache.setLoading(credential, true);
            profile=loadProfile0(gameProfile);
        }
        Thread.currentThread().setName(tempName);
        return ModelManager0.fromUserProfile(profile);
    }
    //Core
    public static UserProfile loadProfile0(GameProfile gameProfile){
        String username=gameProfile.getName();
        String credential=MinecraftUtil.getCredential(gameProfile);
        
        profileCache.setLoading(credential, true);
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
                if(e.getCause()!=null) {
                    logger.warning("Caused By:");
                    logger.warning(e.getCause());
                }
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
            if(!config.enableCape)
                profile0.capeUrl = null;
            profileCache.updateCache(credential, profile0);
            profileCache.setLoading(credential, false);
            logger.info(profile0.toString(profileCache.getExpiry(credential)));
            return profile0;
        }
        logger.info(username+"'s profile not found in load list.");
        
        if(config.enableLocalProfileCache){
            UserProfile profile=profileCache.getLocalProfile(credential);
            if(profile==null)
                logger.info(username+"'s LocalProfile not found.");
            else{
                profileCache.updateCache(credential, profile, false);
                profileCache.setLoading(credential, false);
                logger.info(username+"'s LocalProfile will be used.");
                logger.info(profile.toString(profileCache.getExpiry(credential)));
                return profile;
            }
        }
        profileCache.setLoading(credential, false);
        return null;
    }
    
    //For Skull
    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadProfileFromCache(final GameProfile gameProfile) {
        String username=gameProfile.getName();
        String credential=MinecraftUtil.getCredential(gameProfile);
        
        if(username == null || credential == null)
            return dynamicSkullManager.getTexture(gameProfile);
        if(config.forceUpdateSkull ?profileCache.isReady(credential):profileCache.isExist(credential)){
            UserProfile profile=profileCache.getProfile(credential);
            return ModelManager0.fromUserProfile(profile);
        }
        if (!profileCache.isLoading(credential)) {
            profileCache.setLoading(credential, true);
            Runnable loadThread = () -> {
                String tempName = Thread.currentThread().getName();
                Thread.currentThread().setName(username + "'s skull");
                loadProfile0(gameProfile);//Load in thread
                Thread.currentThread().setName(tempName);
            };
            if (config.forceUpdateSkull) {
                new Thread(loadThread).start();
            } else {
                threadPool.execute(loadThread);
            }
        }
        return Maps.newHashMap();
    }
    
    private static Logger initLogger() {
        Logger logger = new Logger(LOG_FILE);
        logger.info("CustomSkinLoader " + CustomSkinLoader_FULL_VERSION);
        logger.info("DataDir: " + DATA_DIR.getAbsolutePath());
        logger.info("Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
        logger.info("Java Version: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        logger.info("Java VM Version: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
        logger.info("Minecraft: " + MinecraftUtil.getMinecraftMainVersion() + "(" + MinecraftUtil.getMinecraftVersionText() + ")");
        return logger;
    }
}
