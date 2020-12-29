package customskinloader.forge;

import java.io.File;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/**
 * The tweaker for development environment.
 */
public class ForgeDevTweaker implements ITweaker {
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        // ForgeTweaker should be added after FMLTweaker being initialized.
        ((List<String>) Launch.blackboard.get("TweakClasses")).add(ForgeTweaker.class.getName());
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
