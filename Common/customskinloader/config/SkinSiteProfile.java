package customskinloader.config;

public class SkinSiteProfile {
	public String name;
	public String type;
	public String root;
	public String skin;
	public String cape;
	
	public SkinSiteProfile(String name,String type){
		this.name=name;
		this.type=type;
	}
	
	public SkinSiteProfile(String name,String type,String root){
		this.name=name;
		this.type=type;
		this.root=root;
	}
	
	public SkinSiteProfile(String name,String type,String skin,String cape){
		this.name=name;
		this.type=type;
		this.skin=skin;
		this.cape=cape;
	}
}
