package customskinloader.plugins;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import customskinloader.CustomSkinLoader;
import customskinloader.loader.ProfileLoader;

public class PluginsLoader {
    public static HashMap<String, ProfileLoader.IProfileLoader> loadPlugins() {
        File pluginsDir = new File(CustomSkinLoader.DATA_DIR, "Plugins");
        HashMap<String, ProfileLoader.IProfileLoader> pluginLoaders = Maps.newHashMap();
        if (!pluginsDir.isDirectory()) {
            pluginsDir.mkdirs();
            return pluginLoaders;
        }
        for (File plugin : FileUtils.listFiles(pluginsDir, new AbstractFileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.getName().endsWith(".jar") || file.getName().endsWith(".zip");
            }
        }, null)) {
            JarFile jar = null;
            try {
                CustomSkinLoader.logger.info("Start to load the file: " + plugin.getName());
                jar = new JarFile(plugin);
                Attributes attributes = jar.getManifest().getMainAttributes();
                jar.close();
                String pluginClassName = attributes.getValue("CustomSkinLoaderPlugin");
                if (pluginClassName != null) {
                    Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    addURLMethod.setAccessible(true);
                    addURLMethod.invoke(CustomSkinLoader.class.getClassLoader(), plugin.toURI().toURL());
                    ICustomSkinLoaderPlugin cslPlugin = (ICustomSkinLoaderPlugin) Class.forName(pluginClassName, false, CustomSkinLoader.class.getClassLoader()).newInstance();
                    pluginLoaders.put(cslPlugin.getProfileLoader().getName().toLowerCase(), cslPlugin.getProfileLoader());
                    CustomSkinLoader.logger.info("Successfully load the file: " + plugin.getName());
                }
            } catch (Exception e) {
                CustomSkinLoader.logger.warning("Could not load the file: " + plugin.getName());
                CustomSkinLoader.logger.warning(e);
            }
        }
        return pluginLoaders;
    }
}
