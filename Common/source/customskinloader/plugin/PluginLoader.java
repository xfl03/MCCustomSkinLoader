package customskinloader.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.ServiceLoader;

import com.google.common.collect.Lists;
import customskinloader.CustomSkinLoader;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.LegacyLoader;
import customskinloader.loader.MojangAPILoader;
import customskinloader.loader.jsonapi.CustomSkinAPI;
import customskinloader.loader.jsonapi.CustomSkinAPIPlus;
import customskinloader.loader.jsonapi.ElyByAPI;
import customskinloader.loader.jsonapi.GlitchlessAPI;
import customskinloader.loader.jsonapi.UniSkinAPI;
import org.apache.commons.io.FileUtils;

public class PluginLoader {
    public static final ICustomSkinLoaderPlugin[] DEFAULT_PLUGINS = new ICustomSkinLoaderPlugin[] {
        new MojangAPILoader(),
        new LegacyLoader(),
        new JsonAPILoader(new CustomSkinAPI()),
        new JsonAPILoader(new CustomSkinAPIPlus()),
        new JsonAPILoader(new UniSkinAPI()),
        new JsonAPILoader(new ElyByAPI()),
        new JsonAPILoader(new GlitchlessAPI())
    };
    public static final ArrayList<ICustomSkinLoaderPlugin> PLUGINS = loadPlugins();

    private static ArrayList<ICustomSkinLoaderPlugin> loadPlugins() {
        File pluginsDir = new File(CustomSkinLoader.DATA_DIR, "Plugins");
        ArrayList<URL> urls = new ArrayList<>();
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
        ArrayList<ICustomSkinLoaderPlugin> plugins = Lists.newArrayList(DEFAULT_PLUGINS);

        ServiceLoader<ICustomSkinLoaderPlugin> sl = ServiceLoader.load(ICustomSkinLoaderPlugin.class, new URLClassLoader(urls.toArray(new URL[0]), PluginLoader.class.getClassLoader()));
        for (ICustomSkinLoaderPlugin plugin : sl) {
            plugins.add(plugin);
        }
        return plugins;
    }
}
