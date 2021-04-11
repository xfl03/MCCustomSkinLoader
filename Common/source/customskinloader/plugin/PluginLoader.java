package customskinloader.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ServiceLoader;

import customskinloader.CustomSkinLoader;
import customskinloader.plugin.defaults.BlessingSkin;
import customskinloader.plugin.defaults.ElyBy;
import customskinloader.plugin.defaults.GlitchlessGames;
import customskinloader.plugin.defaults.LittleSkin;
import customskinloader.plugin.defaults.LocalSkin;
import customskinloader.plugin.defaults.Mojang;
import customskinloader.plugin.defaults.SkinMe;
import org.apache.commons.io.FileUtils;

public class PluginLoader {
    // This controls the default loading order.
    public static final ICustomSkinLoaderPlugin[] DEFAULT_PLUGINS = new ICustomSkinLoaderPlugin[] {
        new Mojang(), new LittleSkin(), new BlessingSkin(), new ElyBy(), new SkinMe(), new LocalSkin(), new GlitchlessGames()
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
            plugins.put(plugin.getName(), plugin);
        }
        return plugins;
    }

    public static void addPlugin(LinkedHashMap<String, ICustomSkinLoaderPlugin> pluginMap, ICustomSkinLoaderPlugin... plugins) {
        for (ICustomSkinLoaderPlugin plugin : plugins) {
            pluginMap.put(plugin.getName(), plugin);
        }
    }
}
