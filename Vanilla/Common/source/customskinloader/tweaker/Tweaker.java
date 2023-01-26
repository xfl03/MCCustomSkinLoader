package customskinloader.tweaker;

import java.io.File;
import java.util.List;

import customskinloader.log.LogManager;
import customskinloader.log.Logger;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

public class Tweaker implements ITweaker {

    private String[] args;

    public static Logger logger = new Logger();

    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        LogManager.setLogFile(gameDir.toPath().resolve("CustomSkinLoader/CustomSkinLoader.log"));
        logger = LogManager.getLogger("VanillaTweaker");

        logger.info("Using Tweaker");
        logger.info("Tweaker: acceptOptions");

        String[] temp={"--gameDir",gameDir.getAbsolutePath(),"--assetsDir",assetsDir.getAbsolutePath(),"--version",profile};
        this.args=ArrayUtils.addAll(args.toArray(new String[0]), temp);
    }

    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        logger.info("Tweaker: injectIntoClassLoader");
        logger.info("Loaded as a library.");
        Mixins.addConfiguration("mixins.customskinloader.json");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    public String getLaunchTarget() {
        logger.info("Tweaker: getLaunchTarget");
        return "net.minecraft.client.main.Main";
    }

    public String[] getLaunchArguments() {
        logger.info("Tweaker: getLaunchArguments");
        return args;
    }
}
