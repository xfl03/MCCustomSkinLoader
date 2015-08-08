package idv.jlchntoz.tweaker;

import idv.jlchntoz.MainLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class Tweaker implements ITweaker {

	private List args;
	
	private static final File TWEAKER_LOG_FILE = new File("CustomSkinLoader/Tweaker.log");
	public static MainLogger logger = new MainLogger(TWEAKER_LOG_FILE);

	public Tweaker() {
		logger.info("Using Tweaker");
	}

	public void acceptOptions(List args, File gameDir, File assetsDir, String profile) {
		logger.info("Tweaker: acceptOptions");
		this.args = new ArrayList(args);
		this.args.add("--gameDir");
		this.args.add(gameDir.getAbsolutePath());
		this.args.add("--assetsDir");
		this.args.add(assetsDir.getAbsolutePath());
		this.args.add("--version");
		this.args.add(profile);
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
		return (String[])args.toArray(new String[args.size()]);
	}
}
