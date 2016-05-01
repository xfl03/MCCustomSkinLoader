package customskinloader.tweaker;

import customskinloader.Logger;
import customskinloader.utils.MinecraftUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class Tweaker implements ITweaker {

	private List args;
	private String gameDir;
	private String assetsDir;
	private String profile;
	
	public static Logger logger = new Logger();

	public Tweaker() {
		logger.info("Using Tweaker");
	}

	public void acceptOptions(List args, File gameDir, File assetsDir, String profile) {
		MinecraftUtil.minecraftDataFolder=gameDir;
		File tweakerLogFile = new File(MinecraftUtil.getMinecraftDataDir(),"CustomSkinLoader/Tweaker.log");
		logger = new Logger(tweakerLogFile);
		
		logger.info("Using Tweaker");
		logger.info("Tweaker: acceptOptions");
		this.args = new ArrayList(args);
		this.gameDir = gameDir.getAbsolutePath();
		this.assetsDir = assetsDir.getAbsolutePath();
		this.profile = profile;
	}

	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		logger.info("Tweaker: injectIntoClassLoader");
		logger.info("Loaded as a library.");
	}

	public String getLaunchTarget() {
		logger.info("Tweaker: getLaunchTarget");
		return "net.minecraft.client.main.Main";
	}

	public String[] getLaunchArguments() {
		logger.info("Tweaker: getLaunchArguments");
		this.args.add("--gameDir");
		this.args.add(gameDir);
		this.args.add("--assetsDir");
		this.args.add(assetsDir);
		this.args.add("--version");
		this.args.add(profile);
		return (String[])args.toArray(new String[args.size()]);
	}
}
