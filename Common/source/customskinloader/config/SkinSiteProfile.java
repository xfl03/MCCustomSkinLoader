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
}
