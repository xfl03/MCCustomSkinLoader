package customskinloader.plugin;

import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.ProfileLoader;

public interface ICustomSkinLoaderPlugin {
    ProfileLoader.IProfileLoader getProfileLoader();

    SkinSiteProfile getSkinSiteProfile();
}
