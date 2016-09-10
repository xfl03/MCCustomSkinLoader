package customskinloader.config;

public class SkinSiteProfile {
	public String name;
	public String type;
	public Boolean local=null;
	public String root;
	public String skin;
	public String cape;
	public String model;
	public String userAgent;
	
	public static SkinSiteProfile createMojangAPI(String name){
		SkinSiteProfile ssp=new SkinSiteProfile();
		ssp.name=name;
		ssp.type="MojangAPI";
		return ssp;
	}
	public static SkinSiteProfile createCustomSkinAPI(String name,String root){
		SkinSiteProfile ssp=new SkinSiteProfile();
		ssp.name=name;
		ssp.type="CustomSkinAPI";
		ssp.root=root;
		return ssp;
	}
	public static SkinSiteProfile createUniSkinAPI(String name,String root){
		SkinSiteProfile ssp=new SkinSiteProfile();
		ssp.name=name;
		ssp.type="UniSkinAPI";
		ssp.root=root;
		return ssp;
	}
	public static SkinSiteProfile createLegacy(String name,String skin,String cape){
		SkinSiteProfile ssp=new SkinSiteProfile();
		ssp.name=name;
		ssp.type="Legacy";
		ssp.skin=skin;
		ssp.cape=cape;
		return ssp;
	}
}
