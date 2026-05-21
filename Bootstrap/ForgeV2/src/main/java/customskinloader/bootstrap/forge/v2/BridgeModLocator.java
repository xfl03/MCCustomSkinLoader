package customskinloader.bootstrap.forge.v2;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator;

import customskinloader.bootstrap.util.ModLocatorUtils;

public final class BridgeModLocator extends ModsFolderLocator {
    public BridgeModLocator() {
        ModLocatorUtils.initialize(this, ModsFolderLocator.class, FMLPaths.GAMEDIR.get(), "srg");
    }

    @Override
    public String name() {
        return ModLocatorUtils.LOCATOR_NAME;
    }
}
