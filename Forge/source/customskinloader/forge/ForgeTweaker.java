package customskinloader.forge;

import java.io.File;
import java.util.List;

import customskinloader.forge.platform.IFMLPlatform;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class ForgeTweaker implements ITweaker {
    public ForgeTweaker() {
        try {
            IFMLPlatform.FMLPlatformInitializer.initFMLPlatform();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
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
