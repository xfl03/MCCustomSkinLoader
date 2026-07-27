package customskinloader.bootstrap.neoforge.v1;

import customskinloader.bootstrap.ModLoaderInfo;
import customskinloader.bootstrap.util.ModLocatorUtils;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.ModsFolderLocator;

public final class CommonModLocator extends ModsFolderLocator {
    public CommonModLocator() {
        ModLoaderInfo.publish("NeoForge", FMLLoader.versionInfo().neoForgeVersion());
        ModLocatorUtils.initialize(this, ModsFolderLocator.class, FMLPaths.GAMEDIR.get(), "official");
    }

    public String name() {
        return ModLocatorUtils.LOCATOR_NAME;
    }
}
