package customskinloader.config;

public class Config {
	public boolean enable;
	public SkinSiteProfile[] loadlist;
	
	public Config(SkinSiteProfile[] loadlist){
		this.enable=true;
		this.loadlist=loadlist;
	}
}
