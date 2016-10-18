package customskinloader.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import customskinloader.CustomSkinLoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.utils.HttpUtil0;

public class Config {
	//Program
	public String version;
	public boolean enable=true;
	public SkinSiteProfile[] loadlist;
	
	//Function
	public boolean enableSkull=true;
	public boolean enableDynamicSkull=true;
	public boolean enableTransparentSkin=true;
	public boolean ignoreHttpsCertificate=false;
	
	//Cache
	public int cacheExpiry=10;
	public boolean enableUpdateSkull=false;
	public boolean enableLocalProfileCache=false;
	public boolean enableCacheAutoClean=false;
	
	
	//Init config
	public Config(SkinSiteProfile[] loadlist){
		this.version=CustomSkinLoader.CustomSkinLoader_VERSION;
		this.loadlist=loadlist;
	}
	
	public static Config loadConfig0() {
		Config config=loadConfig();
		
		//Init program
		config.loadExtraList();
		config.initLocalFolder();
		if(config.ignoreHttpsCertificate)
			HttpUtil0.ignoreHttpsCertificate();
		
		//Output config
		CustomSkinLoader.logger.info("Enable:"+config.enable+
				", EnableSkull:"+config.enableSkull+
				", EnableDynamicSkull:"+config.enableDynamicSkull+
				", EnableTranSkin:"+config.enableTransparentSkin+
				", IgnoreHttpsCertificate:"+config.ignoreHttpsCertificate+
				", CacheExpiry:"+config.cacheExpiry+
				", EnableUpdateSkull:"+config.enableUpdateSkull+
				", EnableLocalProfileCache:"+config.enableLocalProfileCache+
				", EnableCacheAutoClean:"+config.enableCacheAutoClean+
				", LoadList:"+(config.loadlist==null?0:config.loadlist.length));
		
		//Check config version
		float floatVersion=0f;
		float configVersion=0f;
		try{
			floatVersion=Float.parseFloat(CustomSkinLoader.CustomSkinLoader_VERSION);
			configVersion=Float.parseFloat(config.version);
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
			String json=IOUtils.toString(new FileInputStream(CustomSkinLoader.CONFIG_FILE),Charsets.UTF_8);
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
		ArrayList<SkinSiteProfile> adds=new ArrayList<SkinSiteProfile>();
		File[] files=listAddition.listFiles();
		for(File file:files){
			if(!file.getName().toLowerCase().endsWith(".json")&&!file.getName().toLowerCase().endsWith(".txt"))
				continue;
			try {
				CustomSkinLoader.logger.info("Try to load Extra List.("+file.getName()+")");
				String json=IOUtils.toString(new FileInputStream(file),Charsets.UTF_8);
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
			this.loadlist=ArrayUtils.addAll((SkinSiteProfile[]) adds.toArray(),this.loadlist);
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
			IOUtils.write(json, new FileOutputStream(CustomSkinLoader.CONFIG_FILE),Charsets.UTF_8);
			CustomSkinLoader.logger.info("Successfully "+(update?"update":"create")+" config.");
		} catch (Exception e) {
			CustomSkinLoader.logger.info("Failed to "+(update?"update":"create")+" config.("+e.toString()+")");
		}
	}
}
