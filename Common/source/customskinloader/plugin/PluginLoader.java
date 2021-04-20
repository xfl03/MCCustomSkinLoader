package customskinloader.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ServiceLoader;

import customskinloader.CustomSkinLoader;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.LegacyLoader;
import customskinloader.loader.MojangAPILoader;
import customskinloader.loader.jsonapi.CustomSkinAPI;
import customskinloader.loader.jsonapi.ElyByAPI;
import customskinloader.loader.jsonapi.GlitchlessAPI;
import customskinloader.loader.jsonapi.UniSkinAPI;
import org.apache.commons.io.FileUtils;

public class PluginLoader {
    // This controls the default loading order.
    public static final ICustomSkinLoaderPlugin[] DEFAULT_PLUGINS = new ICustomSkinLoaderPlugin[] {
        new MojangAPILoader.Mojang(),
        new JsonAPILoader(new CustomSkinAPI.LittleSkin()),
        new JsonAPILoader(new CustomSkinAPI.BlessingSkin()),
        new JsonAPILoader(new ElyByAPI.ElyBy()),
        new JsonAPILoader(new UniSkinAPI.SkinMe()),
        new LegacyLoader.LocalSkin(),
        new JsonAPILoader(new GlitchlessAPI.GlitchlessGames())
    };
    public static final LinkedHashMap<String, ICustomSkinLoaderPlugin> PLUGINS = loadPlugins();

    private static LinkedHashMap<String, ICustomSkinLoaderPlugin> loadPlugins() {
        File pluginsDir = new File(CustomSkinLoader.DATA_DIR, "Plugins");
        ArrayList<URL> urls = new ArrayList<URL>();
        if (!pluginsDir.isDirectory()) {
            pluginsDir.mkdirs();
        } else {
            for (File plugin : FileUtils.listFiles(pluginsDir, new String[] {"jar", "zip"}, false)) {
                try {
                    urls.add(plugin.toURI().toURL());
                    CustomSkinLoader.logger.info("Found a jar or zip file: " + plugin.getName());
                } catch (MalformedURLException ignored) {}
            }
        }
        LinkedHashMap<String, ICustomSkinLoaderPlugin> plugins = new LinkedHashMap<>();
        addPlugin(plugins, DEFAULT_PLUGINS);

        ServiceLoader<ICustomSkinLoaderPlugin> sl = ServiceLoader.load(ICustomSkinLoaderPlugin.class, new URLClassLoader(urls.toArray(new URL[0]), PluginLoader.class.getClassLoader()));
        for (ICustomSkinLoaderPlugin plugin : sl) {
            plugins.put(plugin.getLoaderName(), plugin);
        }
        return plugins;
    }

    public static void addPlugin(LinkedHashMap<String, ICustomSkinLoaderPlugin> pluginMap, ICustomSkinLoaderPlugin... plugins) {
        for (ICustomSkinLoaderPlugin plugin : plugins) {
            pluginMap.put(plugin.getLoaderName(), plugin);
        }
    }
}
