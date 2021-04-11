package customskinloader.plugin.defaults;

import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.loader.jsonapi.ElyByAPI;
import customskinloader.plugin.ICustomSkinLoaderPlugin;

public class ElyBy implements ICustomSkinLoaderPlugin {
    @Override
    public ProfileLoader.IProfileLoader getProfileLoader() {
        return new JsonAPILoader(new ElyByAPI());
    }

    @Override
    public String getName() {
        return "ElyBy";
    }

    @Override
    public void updateSkinSiteProfile(SkinSiteProfile ssp) {
        ssp.type = "ElyByAPI";
        ssp.root = "http://skinsystem.ely.by/textures/";
    }
}
