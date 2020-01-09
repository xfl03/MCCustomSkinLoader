package customskinloader.plugin.defaults;

import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.loader.jsonapi.CustomSkinAPI;
import customskinloader.plugin.ICustomSkinLoaderPlugin;

public class LittleSkin implements ICustomSkinLoaderPlugin {
    @Override
    public ProfileLoader.IProfileLoader getProfileLoader() {
        return new JsonAPILoader(new CustomSkinAPI());
    }

    @Override
    public SkinSiteProfile getSkinSiteProfile() {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = "LittleSkin";
        ssp.type = "CustomSkinAPI";
        ssp.root = "https://littleskin.cn/";
        return ssp;
    }
}
