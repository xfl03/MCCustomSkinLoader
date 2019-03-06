package customskinloader.plugin;

import customskinloader.loader.ProfileLoader;

public interface ICustomSkinLoaderPlugin {
    ProfileLoader.IProfileLoader getProfileLoader();
}
