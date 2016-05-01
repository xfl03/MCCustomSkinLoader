package customskinloader.tweaker;

import customskinloader.Logger;
import customskinloader.utils.MinecraftUtil;

import java.io.File;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class ForgeTweaker implements ITweaker {
	
	public static Logger logger = new Logger();
	
	public ForgeTweaker() {
	}

	public void acceptOptions(List args, File gameDir, File assetsDir, String profile) {
		MinecraftUtil.minecraftDataFolder=gameDir;
		File tweakerLogFile = new File(MinecraftUtil.getMinecraftDataDir(),"CustomSkinLoader/Tweaker.log");
		logger = new Logger(tweakerLogFile);
		
		logger.info("Using ForgeTweaker");
		logger.info("ForgeTweaker: acceptOptions");
	}

	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		logger.info("ForgeTweaker: injectIntoClassLoader");
		classLoader.registerTransformer("customskinloader.tweaker.ClassTransformer");
		logger.info("ClassTransformer Registered");
	}

	public String getLaunchTarget() {
		logger.info("ForgeTweaker: getLaunchTarget");
		return "net.minecraft.client.main.Main";
	}

	public String[] getLaunchArguments() {
		logger.info("ForgeTweaker: getLaunchArguments");
		return new String[0];
	}
}
