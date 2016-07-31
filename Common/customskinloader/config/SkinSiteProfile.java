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
		return createCustomSkinAPI(name,false,root);
	}
	public static SkinSiteProfile createCustomSkinAPI(String name,boolean local,String root){
		SkinSiteProfile ssp=new SkinSiteProfile();
		ssp.name=name;
		ssp.type="CustomSkinAPI";
		if(local)
			ssp.local=true;
		ssp.root=root;
		return ssp;
	}
	public static SkinSiteProfile createUniSkinAPI(String name,String root){
		return createUniSkinAPI(name,false,root);
	}
	public static SkinSiteProfile createUniSkinAPI(String name,boolean local,String root){
		SkinSiteProfile ssp=new SkinSiteProfile();
		ssp.name=name;
		ssp.type="UniSkinAPI";
		if(local)
			ssp.local=true;
		ssp.root=root;
		return ssp;
	}
	public static SkinSiteProfile createLegacy(String name,String skin,String cape){
		return createLegacy(name,false,skin,cape);
	}
	public static SkinSiteProfile createLegacy(String name,boolean local,String skin,String cape){
		SkinSiteProfile ssp=new SkinSiteProfile();
		ssp.name=name;
		ssp.type="Legacy";
		if(local)
			ssp.local=true;
		ssp.skin=skin;
		ssp.cape=cape;
		return ssp;
	}
}
