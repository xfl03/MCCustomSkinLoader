package customskinloader.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;

import org.apache.commons.io.FileUtils;

import customskinloader.CustomSkinLoader;
import customskinloader.loader.ProfileLoader;

public class PluginLoader {
    public static HashMap<String, ProfileLoader.IProfileLoader> loadPlugins() {
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
        ServiceLoader<ICustomSkinLoaderPlugin> sl = ServiceLoader.load(ICustomSkinLoaderPlugin.class, new URLClassLoader(urls.toArray(new URL[0]), PluginLoader.class.getClassLoader()));
        HashMap<String, ProfileLoader.IProfileLoader> profileLoaders = new HashMap<String, ProfileLoader.IProfileLoader>();
        for (ICustomSkinLoaderPlugin cslPlugin : sl) {
            ProfileLoader.IProfileLoader profileLoader = cslPlugin.getProfileLoader();
            profileLoaders.put(profileLoader.getName().toLowerCase(), profileLoader);
            CustomSkinLoader.logger.info("Add profile loader: " + profileLoader.getName());
        }
        return profileLoaders;
    }
}
