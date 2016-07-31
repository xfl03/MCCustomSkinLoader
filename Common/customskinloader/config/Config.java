package customskinloader.config;

import customskinloader.CustomSkinLoader;

public class Config {
	public String version;
	public boolean enable=true;
	public boolean enableSkull=true;
	public boolean enableTransparentSkin=false;
	public int cacheExpiry=10;
	public boolean enableUpdateSkull=false;
	public boolean enableLocalProfileCache=false;
	public SkinSiteProfile[] loadlist;
	
	//Init config
	public Config(SkinSiteProfile[] loadlist){
		this.version=CustomSkinLoader.CustomSkinLoader_VERSION;
		this.loadlist=loadlist;
	}
}
