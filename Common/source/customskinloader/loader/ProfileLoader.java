package customskinloader.loader;

import java.util.HashMap;

import com.mojang.authlib.GameProfile;

import customskinloader.config.SkinSiteProfile;
import customskinloader.plugins.PluginsLoader;
import customskinloader.profile.UserProfile;

public class ProfileLoader {
    private static final IProfileLoader[] DEFAULT_LOADERS={
            new MojangAPILoader(),
            new JsonAPILoader(JsonAPILoader.Type.CustomSkinAPI),
            new JsonAPILoader(JsonAPILoader.Type.CustomSkinAPIPlus),
            new JsonAPILoader(JsonAPILoader.Type.UniSkinAPI),
            new JsonAPILoader(JsonAPILoader.Type.ElyByAPI),
            new LegacyLoader()};
    
    public static final HashMap<String,IProfileLoader> LOADERS=initLoaders();
    
    private static HashMap<String, IProfileLoader> initLoaders() {
        HashMap<String, IProfileLoader> loaders=new HashMap<String, IProfileLoader>();
        for(IProfileLoader loader:DEFAULT_LOADERS){
            loaders.put(loader.getName().toLowerCase(), loader);
        }
        loaders.putAll(PluginsLoader.loadPlugins());
        return loaders;
    }
    
    public interface IProfileLoader {
        UserProfile loadProfile(SkinSiteProfile ssp,GameProfile gameProfile) throws Exception;
        boolean compare(SkinSiteProfile ssp0,SkinSiteProfile ssp1);
        String getName();
        void initLocalFolder(SkinSiteProfile ssp);
    }
}
