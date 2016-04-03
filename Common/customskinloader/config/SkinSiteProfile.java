package customskinloader.config;

public class SkinSiteProfile {
	public String name;
	public String type;
	public String root;
	public String skin;
	public String cape;
	public String model;
	
	//For MojangAPI
	public SkinSiteProfile(String name,String type){
		this.name=name;
		this.type=type;
	}
	
	//For CSL/USM API
	public SkinSiteProfile(String name,String type,String root){
		this.name=name;
		this.type=type;
		this.root=root;
	}
	
	//for Legacy
	public SkinSiteProfile(String name,String type,String skin,String cape,String model){
		this.name=name;
		this.type=type;
		this.skin=skin;
		this.cape=cape;
		this.model=model;
	}
}
