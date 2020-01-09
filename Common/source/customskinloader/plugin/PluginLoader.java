package customskinloader.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.ServiceLoader;

import customskinloader.CustomSkinLoader;
import org.apache.commons.io.FileUtils;

public class PluginLoader {
    public static final ServiceLoader<ICustomSkinLoaderPlugin> PLUGINS = loadPlugins();

    private static ServiceLoader<ICustomSkinLoaderPlugin> loadPlugins() {
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
        return ServiceLoader.load(ICustomSkinLoaderPlugin.class, new URLClassLoader(urls.toArray(new URL[0]), PluginLoader.class.getClassLoader()));
    }
}
