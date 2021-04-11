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
    public String getName() {
        return "SkinMe";
    }

    @Override
    public void updateSkinSiteProfile(SkinSiteProfile ssp) {
        ssp.type = "UniSkinAPI";
        ssp.root = "http://www.skinme.cc/uniskin/";
    }
}
