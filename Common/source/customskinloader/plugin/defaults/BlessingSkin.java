package customskinloader.plugin.defaults;

import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.loader.jsonapi.CustomSkinAPI;
import customskinloader.plugin.ICustomSkinLoaderPlugin;

public class BlessingSkin implements ICustomSkinLoaderPlugin {
    @Override
    public ProfileLoader.IProfileLoader getProfileLoader() {
        return new JsonAPILoader(new CustomSkinAPI());
    }

    @Override
    public SkinSiteProfile getSkinSiteProfile() {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = "BlessingSkin";
        ssp.type = "CustomSkinAPI";
        ssp.root = "http://skin.prinzeugen.net/";
        return ssp;
    }
}
