package customskinloader.tweaker;

import customskinloader.Logger;
import customskinloader.utils.MinecraftUtil;

import java.io.File;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class ForgeTweaker implements ITweaker {
	
	private static final File TWEAKER_LOG_FILE = new File(MinecraftUtil.getMinecraftDataDir(),"CustomSkinLoader/Tweaker.log");
	public static Logger logger = new Logger(TWEAKER_LOG_FILE);
	
	public ForgeTweaker() {
		logger.info("Using ForgeTweaker");
	}

	public void acceptOptions(List args, File gameDir, File assetsDir, String profile) {
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
