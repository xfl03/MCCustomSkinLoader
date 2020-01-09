package customskinloader.plugin.defaults;

import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.LegacyLoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;

public class LocalSkin implements ICustomSkinLoaderPlugin {
    @Override
    public ProfileLoader.IProfileLoader getProfileLoader() {
        return new LegacyLoader();
    }

    @Override
    public SkinSiteProfile getSkinSiteProfile() {
        SkinSiteProfile ssp = new SkinSiteProfile();
        ssp.name = "LocalSkin";
        ssp.type = "Legacy";
        ssp.checkPNG = false;
        ssp.skin = "LocalSkin/skins/{USERNAME}.png";
        ssp.model = "auto";
        ssp.cape = "LocalSkin/capes/{USERNAME}.png";
        ssp.elytra = "LocalSkin/elytras/{USERNAME}.png";
        return ssp;
    }
}
