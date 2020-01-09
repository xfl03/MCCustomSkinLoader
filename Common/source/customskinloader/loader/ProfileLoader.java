package customskinloader.loader;

import java.util.HashMap;

import com.mojang.authlib.GameProfile;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.plugin.PluginLoader;
import customskinloader.profile.UserProfile;

public class ProfileLoader {
    public static final HashMap<String,IProfileLoader> LOADERS=initLoaders();
    
    private static HashMap<String, IProfileLoader> initLoaders() {
        HashMap<String, ProfileLoader.IProfileLoader> profileLoaders = new HashMap<String, ProfileLoader.IProfileLoader>();
        for (ICustomSkinLoaderPlugin plugin : PluginLoader.PLUGINS) {
            ProfileLoader.IProfileLoader profileLoader = plugin.getProfileLoader();
            if (profileLoader != null) {
                profileLoaders.put(profileLoader.getName().toLowerCase(), profileLoader);
                CustomSkinLoader.logger.info("Add a profile loader: " + profileLoader.getName());
            }
        }
        return profileLoaders;
    }
    
    public interface IProfileLoader {
        UserProfile loadProfile(SkinSiteProfile ssp,GameProfile gameProfile) throws Exception;
        boolean compare(SkinSiteProfile ssp0,SkinSiteProfile ssp1);
        String getName();
        void init(SkinSiteProfile ssp);
    }
}
