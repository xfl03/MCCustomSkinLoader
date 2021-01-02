package customskinloader.tweaker;

import java.io.File;
import java.util.List;

import customskinloader.Logger;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

public class Tweaker implements ITweaker {

    private String[] args;

    public static Logger logger = new Logger();

    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        File tweakerLogFile = new File(gameDir,"./CustomSkinLoader/Tweaker.log");
        logger = new Logger(tweakerLogFile);

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
