package customskinloader.loader;

import java.util.HashMap;

import com.mojang.authlib.GameProfile;

import customskinloader.config.SkinSiteProfile;
import customskinloader.plugin.PluginLoader;
import customskinloader.profile.UserProfile;

public class ProfileLoader {
    private static final IProfileLoader[] DEFAULT_LOADERS={
            new MojangAPILoader(),
            new JsonAPILoader(JsonAPILoader.Type.CustomSkinAPI),
            new JsonAPILoader(JsonAPILoader.Type.CustomSkinAPIPlus),
            new JsonAPILoader(JsonAPILoader.Type.UniSkinAPI),
            new JsonAPILoader(JsonAPILoader.Type.ElyByAPI),
            new JsonAPILoader(JsonAPILoader.Type.GlitchlessAPI),
            new LegacyLoader()};
    
    public static final HashMap<String,IProfileLoader> LOADERS=initLoaders();
    
    private static HashMap<String, IProfileLoader> initLoaders() {
        HashMap<String, IProfileLoader> loaders=new HashMap<String, IProfileLoader>();
        for(IProfileLoader loader:DEFAULT_LOADERS){
            loaders.put(loader.getName().toLowerCase(), loader);
        }
        loaders.putAll(PluginLoader.loadPlugins());
        return loaders;
    }
    
    public interface IProfileLoader {
        UserProfile loadProfile(SkinSiteProfile ssp,GameProfile gameProfile) throws Exception;
        boolean compare(SkinSiteProfile ssp0,SkinSiteProfile ssp1);
        String getName();
        void init(SkinSiteProfile ssp);
    }
}
