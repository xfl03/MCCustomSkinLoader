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
    public String getName() {
        return "LocalSkin";
    }

    @Override
    public void updateSkinSiteProfile(SkinSiteProfile ssp) {
        ssp.type = "Legacy";
        if (ssp.checkPNG == null) ssp.checkPNG = false;
        if (ssp.skin     == null) ssp.skin     = "LocalSkin/skins/{USERNAME}.png";
        if (ssp.model    == null) ssp.model    = "auto";
        if (ssp.cape     == null) ssp.cape     = "LocalSkin/capes/{USERNAME}.png";
        if (ssp.elytra   == null) ssp.elytra   = "LocalSkin/elytras/{USERNAME}.png";
    }
}
