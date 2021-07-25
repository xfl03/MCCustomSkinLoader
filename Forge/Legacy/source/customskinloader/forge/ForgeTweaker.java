package customskinloader.forge;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import customskinloader.Logger;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class ForgeTweaker implements ITweaker {
    public static Logger logger = new Logger(new File("./CustomSkinLoader/ForgePlugin.log"));

    private final static String FML_PLATFORM_INITIALIZER = "customskinloader.forge.platform.IFMLPlatform$FMLPlatformInitializer";

    @SuppressWarnings("unchecked")
    public ForgeTweaker() throws Exception {
        // FML is loaded by LaunchClassLoader but we are in AppClassLoader.
        if (!this.getClass().getClassLoader().equals(Launch.classLoader)) {
            Field field = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
            field.setAccessible(true);

            String exclusionName = this.getClass().getName().substring(0, this.getClass().getName().lastIndexOf("."));
            ((Set<String>) field.get(Launch.classLoader)).remove(exclusionName);
            Launch.classLoader.addClassLoaderExclusion(Logger.class.getName());
            Launch.classLoader.addTransformerExclusion(exclusionName);
            Launch.classLoader.loadClass(FML_PLATFORM_INITIALIZER).getMethod("initFMLPlatform").invoke(null);
        }
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

    }

    @Override
    public String getLaunchTarget() {
        return "";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
