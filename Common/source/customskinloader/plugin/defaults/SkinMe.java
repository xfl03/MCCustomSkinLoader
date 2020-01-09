package customskinloader.plugin.defaults;

import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.loader.jsonapi.UniSkinAPI;
import customskinloader.plugin.ICustomSkinLoaderPlugin;

public class SkinMe implements ICustomSkinLoaderPlugin {
    @Override
    public ProfileLoader.IProfileLoader getProfileLoader() {
        return new JsonAPILoader(new UniSkinAPI());
    }

    @Override
    public SkinSiteProfile getSkinSiteProfile() {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = "SkinMe";
        ssp.type = "UniSkinAPI";
        ssp.root = "http://www.skinme.cc/uniskin/";
        return ssp;
    }
}
