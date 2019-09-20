package customskinloader.config;

public class SkinSiteProfile {
    //Common
    public String name;
    public String type;
    public String userAgent;

    //Mojang API
    public String apiRoot;
    public String sessionRoot;

    //Json API
    public String root;

    //Legacy
    public Boolean checkPNG;//Not suitable for local skin
    public String skin;
    public String model;
    public String cape;
    public String elytra;

    public static SkinSiteProfile createMojangAPI(String name) {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = name;
        ssp.type = "MojangAPI";
        return ssp;
    }

    public static SkinSiteProfile createCustomSkinAPI(String name, String root) {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = name;
        ssp.type = "CustomSkinAPI";
        ssp.root = root;
        return ssp;
    }

    public static SkinSiteProfile creatCustomSkinAPIPlus(String name, String root) {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = name;
        ssp.type = "CustomSkinAPIPlus";
        ssp.root = root;
        return ssp;
    }

    public static SkinSiteProfile createGlitchlessAPI(String name, String root) {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = name;
        ssp.type = "GlitchlessAPI";
        ssp.root = root;
        return ssp;
    }

    public static SkinSiteProfile createUniSkinAPI(String name, String root) {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = name;
        ssp.type = "UniSkinAPI";
        ssp.root = root;
        return ssp;
    }

    public static SkinSiteProfile createLegacy(String name, String skin, String cape, String elytra) {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = name;
        ssp.type = "Legacy";
        ssp.checkPNG = false;
        ssp.skin = skin;
        ssp.model = "auto";
        ssp.cape = cape;
        ssp.elytra = elytra;
        return ssp;
    }

    public static SkinSiteProfile createElyByAPI(String name) {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = name;
        ssp.type = "ElyByAPI";
        return ssp;
    }
}
