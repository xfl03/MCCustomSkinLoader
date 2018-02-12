package customskinloader.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

import customskinloader.CustomSkinLoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;

public class Config {
    //Program
    public String version;
    public List<SkinSiteProfile> loadlist;
    
    //Function
    public boolean enableDynamicSkull=true;
    public boolean enableTransparentSkin=true;
    public boolean ignoreHttpsCertificate=false;
    public boolean forceLoadAllTextures=false;
    
    //Cache
    public int cacheExpiry=20;
    public boolean enableUpdateSkull=false;
    public boolean enableLocalProfileCache=false;
    public boolean enableCacheAutoClean=false;
    
    
    //Init config
    public Config(SkinSiteProfile[] loadlist){
        this.version=CustomSkinLoader.CustomSkinLoader_VERSION;
        this.loadlist=Arrays.asList(loadlist);
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
        if(config.ignoreHttpsCertificate)
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
        
        //Output config
        CustomSkinLoader.logger.info("EnableDynamicSkull:"+config.enableDynamicSkull+
                ", EnableTranSkin:"+config.enableTransparentSkin+
                ", IgnoreHttpsCertificate:"+config.ignoreHttpsCertificate+
                ", CacheExpiry:"+config.cacheExpiry+
                ", EnableUpdateSkull:"+config.enableUpdateSkull+
                ", EnableLocalProfileCache:"+config.enableLocalProfileCache+
                ", EnableCacheAutoClean:"+config.enableCacheAutoClean+
                ", LoadList:"+(config.loadlist==null?0:config.loadlist.size()));
        
        //Check config version
        float floatVersion=0f;
        float configVersion=0f;
        try{
            floatVersion=Float.parseFloat(CustomSkinLoader.CustomSkinLoader_VERSION);
            configVersion=Float.parseFloat(config.version);
            if(configVersion==15.1f)//To avoid some bug
                configVersion=14.6f;
        }catch (Exception e){
            CustomSkinLoader.logger.warning("Exception occurs while parsing version: "+e.toString());
        }
        if(config.version==null || configVersion==0f || floatVersion > configVersion){
            CustomSkinLoader.logger.info("Config File is out of date: "+config.version);
            config.version=CustomSkinLoader.CustomSkinLoader_VERSION;
            writeConfig(config,true);
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
            String json=FileUtils.readFileToString(CustomSkinLoader.CONFIG_FILE,Charsets.UTF_8);
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
        for(File file:files){
            if(!file.getName().toLowerCase().endsWith(".json")&&!file.getName().toLowerCase().endsWith(".txt"))
                continue;
            try {
                CustomSkinLoader.logger.info("Try to load Extra List.("+file.getName()+")");
                String json=FileUtils.readFileToString(file,Charsets.UTF_8);
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
            loader.initLocalFolder(ssp);
        }
    }

    private static Config initConfig() {
        Config config=new Config(CustomSkinLoader.DEFAULT_LOAD_LIST);
        writeConfig(config,false);
        return config;
    }
    private static void writeConfig(Config config,boolean update){
        String json=CustomSkinLoader.GSON.toJson(config);
        if(CustomSkinLoader.CONFIG_FILE.exists())
            CustomSkinLoader.CONFIG_FILE.delete();
        try {
            CustomSkinLoader.CONFIG_FILE.createNewFile();
            FileUtils.write(CustomSkinLoader.CONFIG_FILE, json, Charsets.UTF_8);
            CustomSkinLoader.logger.info("Successfully "+(update?"update":"create")+" config.");
        } catch (Exception e) {
            CustomSkinLoader.logger.info("Failed to "+(update?"update":"create")+" config.("+e.toString()+")");
        }
    }
}
