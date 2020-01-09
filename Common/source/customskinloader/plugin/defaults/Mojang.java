package customskinloader.plugin.defaults;

import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.MojangAPILoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;

public class Mojang implements ICustomSkinLoaderPlugin {
    @Override
    public ProfileLoader.IProfileLoader getProfileLoader() {
        return new MojangAPILoader();
    }

    @Override
    public SkinSiteProfile getSkinSiteProfile() {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = "Mojang";
        ssp.type = "MojangAPI";
        return ssp;
    }
}
