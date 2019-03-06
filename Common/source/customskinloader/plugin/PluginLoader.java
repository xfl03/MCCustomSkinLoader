package customskinloader.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.ServiceLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;

import com.google.common.collect.Maps;

import customskinloader.CustomSkinLoader;
import customskinloader.loader.ProfileLoader;

public class PluginLoader {
    private static PluginClassLoader pcl = new PluginClassLoader(((URLClassLoader) PluginLoader.class.getClassLoader()).getURLs(), PluginLoader.class.getClassLoader());
    
    public static HashMap<String, ProfileLoader.IProfileLoader> loadPlugins() {
        File pluginsDir = new File(CustomSkinLoader.DATA_DIR, "Plugins");
        if (!pluginsDir.isDirectory()) {
            pluginsDir.mkdirs();
        } else {
            for (File plugin : FileUtils.listFiles(pluginsDir, new AbstractFileFilter() {
                @Override
                public boolean accept(final File file) {
                    String fileName = file.getName().toLowerCase();
                    return fileName.endsWith(".jar") || fileName.endsWith(".zip");
                }
            }, null)) {
                try {
                    pcl.addURL(plugin.toURI().toURL());
                    CustomSkinLoader.logger.info("Successfully to load file: " + plugin.getName());
                } catch (MalformedURLException e) {
                    CustomSkinLoader.logger.warning("Could not load file: " + plugin.getName());
                }
            }
        }
        ServiceLoader<ICustomSkinLoaderPlugin> sl = ServiceLoader.load(ICustomSkinLoaderPlugin.class, pcl);
        HashMap<String, ProfileLoader.IProfileLoader> profileLoaders = Maps.newHashMap();
        for (ICustomSkinLoaderPlugin cslPlugin : sl) {
            ProfileLoader.IProfileLoader profileLoader = cslPlugin.getProfileLoader();
            profileLoaders.put(profileLoader.getName().toLowerCase(), profileLoader);
            CustomSkinLoader.logger.info("Add profile loader: " + profileLoader.getName());
        }
        return profileLoaders;
    }
}
