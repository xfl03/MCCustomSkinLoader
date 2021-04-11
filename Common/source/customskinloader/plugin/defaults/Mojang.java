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
    public String getName() {
        return "Mojang";
    }

    @Override
    public void updateSkinSiteProfile(SkinSiteProfile ssp) {
        ssp.type        = "MojangAPI";
        ssp.apiRoot     = "https://api.mojang.com/";
        ssp.sessionRoot = "https://sessionserver.mojang.com/";
    }
}
