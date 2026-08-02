package customskinloader.bootstrap.forge.v2;

import customskinloader.bootstrap.util.ModLocatorUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator;

public final class CommonModLocator extends ModsFolderLocator {
    public CommonModLocator() {
        ModLocatorUtils.initialize(this, ModsFolderLocator.class, FMLPaths.GAMEDIR.get(), "srg");
    }

    @Override
    public String name() {
        return ModLocatorUtils.LOCATOR_NAME;
    }
}
