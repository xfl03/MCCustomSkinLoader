package customskinloader.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import customskinloader.CustomSkinLoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.plugin.PluginLoader;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;
import customskinloader.utils.Version;
import org.apache.commons.io.FileUtils;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Config {
    //Program
    public String version;
    public int buildNumber;
    public List<SkinSiteProfile> loadlist;

    //Function
    public boolean enableDynamicSkull=true;
    public boolean enableTransparentSkin=true;
    public boolean forceIgnoreHttpsCertificate = false;
    public boolean forceLoadAllTextures=false;
    public boolean enableCape = true;
    public boolean forceFillSkullNBT = false;
    public int threadPoolSize = 3;
    public int retryTime = 1;

    //Profile Cache
    public int cacheExpiry=30;
    public boolean forceUpdateSkull = false;
    public boolean enableLocalProfileCache=false;

    //Network Cache
    public boolean enableCacheAutoClean=false;
    public boolean forceDisableCache = false;

    // Used by Gson to create an instance with default value.
    public Config() {
        this(new ArrayList<>());
    }

    //Init config
    public Config(List<SkinSiteProfile> loadlist){
        this.version=CustomSkinLoader.CustomSkinLoader_VERSION;
        this.buildNumber=CustomSkinLoader.CustomSkinLoader_BUILD_NUMBER;
        this.loadlist=loadlist;
    }

    public static Config loadConfig0() {
        Config config=loadConfig();

        //LoadList null checker
        if(config.loadlist==null){
            config.loadlist=new ArrayList<SkinSiteProfile>();
        }else{
            for(int i=0;i<config.loadlist.size();i++){
                if(config.loadlist.get(i)==null)
                    config.loadlist.remove(i--);
            }
        }

        //Init program
        config.loadExtraList();
        config.initLocalFolder();
        config.threadPoolSize = Math.max(config.threadPoolSize, 1);
        config.retryTime = Math.max(config.retryTime, 0);
        if(config.forceIgnoreHttpsCertificate)
            HttpUtil0.ignoreHttpsCertificate();
        if(config.enableCacheAutoClean && !config.enableLocalProfileCache){
            try{
                FileUtils.deleteDirectory(HttpRequestUtil.CACHE_DIR);
                FileUtils.deleteDirectory(HttpTextureUtil.getCacheDir());
                CustomSkinLoader.logger.info("Successfully cleaned cache.");
            }catch(Exception e){
                CustomSkinLoader.logger.warning("Exception occurs while cleaning cache: "+e.toString());
            }
        }

        //Check config version
        Version configVersion = Version.of(config.version);
        if (CustomSkinLoader.CustomSkinLoader_BUILD_NUMBER == 0 // Custom builds
            || configVersion.compareTo(CustomSkinLoader.CustomSkinLoader_VERSION) < 0
            || config.buildNumber < CustomSkinLoader.CustomSkinLoader_BUILD_NUMBER) {
            CustomSkinLoader.logger.info("Config File is out of date: " + config.version + ", build number: " + config.buildNumber);
            config.version = CustomSkinLoader.CustomSkinLoader_VERSION;
            config.buildNumber = CustomSkinLoader.CustomSkinLoader_BUILD_NUMBER;
        }
        writeConfig(config, true);

        //Output config
        for (Field field : config.getClass().getDeclaredFields()) {
            try {
                Object value = field.get(config);
                CustomSkinLoader.logger.info(field.getName() + " : " + value);
            } catch (Exception ignored) { }
        }

        return config;
    }

    private static Config loadConfig() {
        CustomSkinLoader.logger.info("Config File: "+CustomSkinLoader.CONFIG_FILE.getAbsolutePath());
        if(!CustomSkinLoader.CONFIG_FILE.exists()){
            CustomSkinLoader.logger.info("Config file not found, use default instead.");
            return initConfig();
        }
        try {
            CustomSkinLoader.logger.info("Try to load config.");
            String json=FileUtils.readFileToString(CustomSkinLoader.CONFIG_FILE, "UTF-8");
            Config config=CustomSkinLoader.GSON.fromJson(json, Config.class);
            CustomSkinLoader.logger.info("Successfully load config.");
            return config;
        }catch (Exception e) {
            CustomSkinLoader.logger.info("Failed to load config, use default instead.("+e.toString()+")");
            File brokenFile=new File(CustomSkinLoader.DATA_DIR,"BROKEN-CustomSkinLoader.json");
            if(brokenFile.exists())
                brokenFile.delete();
            CustomSkinLoader.CONFIG_FILE.renameTo(brokenFile);
            return initConfig();
        }
    }

    private void loadExtraList(){
        File listAddition=new File(CustomSkinLoader.DATA_DIR,"ExtraList");
        if(!listAddition.isDirectory()){
            listAddition.mkdirs();
            return;
        }
        List<SkinSiteProfile> adds=new ArrayList<SkinSiteProfile>();
        File[] files=listAddition.listFiles();
        for(File file: files != null ? files : new File[0]){
            if(!file.getName().toLowerCase().endsWith(".json")&&!file.getName().toLowerCase().endsWith(".txt"))
                continue;
            try {
                CustomSkinLoader.logger.info("Try to load Extra List.("+file.getName()+")");
                String json=FileUtils.readFileToString(file, "UTF-8");
                SkinSiteProfile ssp=CustomSkinLoader.GSON.fromJson(json, SkinSiteProfile.class);
                CustomSkinLoader.logger.info("Successfully load Extra List.");
                file.delete();
                ProfileLoader.IProfileLoader loader=ProfileLoader.LOADERS.get(ssp.type.toLowerCase());
                if(loader==null){
                    CustomSkinLoader.logger.info("Extra List will be ignore: Type '"+ssp.type+"' is not defined.");
                    continue;
                }
                boolean duplicate=false;
                for(SkinSiteProfile ssp0:this.loadlist){
                    if(!ssp0.type.equalsIgnoreCase(ssp.type))
                        continue;
                    if(loader.compare(ssp0, ssp)){
                        duplicate=true;
                        break;
                    }
                }
                if(!duplicate){
                    adds.add(ssp);
                    CustomSkinLoader.logger.info("Successfully apply Extra List.("+ssp.name+")");
                }else{
                    CustomSkinLoader.logger.info("Extra List will be ignore: Duplicate.("+ssp.name+")");
                }
            }catch (Exception e) {
                CustomSkinLoader.logger.info("Failed to load Extra List.("+e.toString()+")");
            }
        }
        if(adds.size()!=0){
            adds.addAll(this.loadlist);
            this.loadlist=adds;
            writeConfig(this,true);
        }
    }

    private void initLocalFolder(){
        for(SkinSiteProfile ssp:this.loadlist){
            ProfileLoader.IProfileLoader loader=ProfileLoader.LOADERS.get(ssp.type.toLowerCase());
            if(loader==null)
                continue;
            loader.init(ssp);
        }
    }

    // The config file does not exist or was broken.
    private static Config initConfig() {
        List<ICustomSkinLoaderPlugin.IDefaultProfile> profiles = new ArrayList<>();
        for (ICustomSkinLoaderPlugin plugin : PluginLoader.PLUGINS) {
            List<ICustomSkinLoaderPlugin.IDefaultProfile> defaultProfiles = plugin.getDefaultProfiles();
            if (defaultProfiles != null) {
                profiles.addAll(defaultProfiles);
            }
        }
        profiles.sort(Comparator.comparingInt(ICustomSkinLoaderPlugin.IDefaultProfile::getPriority));

        List<SkinSiteProfile> loadlist = new ArrayList<>();
        for (ICustomSkinLoaderPlugin.IDefaultProfile profile : profiles) {
            SkinSiteProfile ssp = new SkinSiteProfile();
            ssp.name = profile.getName();
            profile.updateSkinSiteProfile(ssp);
            loadlist.add(ssp);
        }

        Config config = new Config(loadlist);
        writeConfig(config, false);
        return config;
    }
    private static void writeConfig(Config config, boolean update){
        if (update && config.loadlist != null) { // Update loadlist, complete missing elements
            for (ICustomSkinLoaderPlugin plugin : PluginLoader.PLUGINS) {
                List<ICustomSkinLoaderPlugin.IDefaultProfile> profiles = plugin.getDefaultProfiles();
                if (profiles != null) {
                    for (ICustomSkinLoaderPlugin.IDefaultProfile profile : profiles) {
                        for (SkinSiteProfile ssp : config.loadlist) {
                            if (profile.getName().equals(ssp.name)) {
                                profile.updateSkinSiteProfile(ssp);
                            }
                        }
                    }
                }
            }
        }

        String json=CustomSkinLoader.GSON.toJson(config);
        if(CustomSkinLoader.CONFIG_FILE.exists())
            CustomSkinLoader.CONFIG_FILE.delete();
        try {
            CustomSkinLoader.CONFIG_FILE.createNewFile();
            FileUtils.write(CustomSkinLoader.CONFIG_FILE, json, "UTF-8");
            CustomSkinLoader.logger.info("Successfully "+(update?"update":"create")+" config.");
        } catch (Exception e) {
            CustomSkinLoader.logger.info("Failed to "+(update?"update":"create")+" config.("+e.toString()+")");
        }
    }
}
