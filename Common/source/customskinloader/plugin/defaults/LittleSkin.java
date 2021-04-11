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
    public String getName() {
        return "LittleSkin";
    }

    @Override
    public void updateSkinSiteProfile(SkinSiteProfile ssp) {
        ssp.type = "CustomSkinAPI";
        ssp.root = "https://littlesk.in/csl/";
    }
}
