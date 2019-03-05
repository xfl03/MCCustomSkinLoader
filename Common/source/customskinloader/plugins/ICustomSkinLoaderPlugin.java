package customskinloader.plugins;

import customskinloader.loader.ProfileLoader;

public interface ICustomSkinLoaderPlugin {
    ProfileLoader.IProfileLoader getProfileLoader();
}
