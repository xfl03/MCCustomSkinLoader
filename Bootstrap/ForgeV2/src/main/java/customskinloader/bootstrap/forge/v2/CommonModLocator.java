package customskinloader.bootstrap.forge.v2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator;

import customskinloader.bootstrap.ModLoaderInfo;
import customskinloader.bootstrap.util.ModLocatorUtils;

public final class CommonModLocator extends ModsFolderLocator {
    public CommonModLocator() {
        ModLoaderInfo.publish("Forge", getForgeVersion());
        ModLocatorUtils.initialize(this, ModsFolderLocator.class, FMLPaths.GAMEDIR.get(), "srg");
    }

    private static String getForgeVersion() {
        try {
            Method versionInfoMethod = FMLLoader.class.getMethod("versionInfo");
            Object versionInfo = versionInfoMethod.invoke(null);
            return versionInfo.getClass().getMethod("forgeVersion").invoke(versionInfo).toString();
        } catch (NoSuchMethodException ignored) {
            try {
                Field versionField = FMLLoader.class.getDeclaredField("forgeVersion");
                versionField.setAccessible(true);
                return versionField.get(null).toString();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not read the Forge version", exception);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not read the Forge version", exception);
        }
    }

    @Override
    public String name() {
        return ModLocatorUtils.LOCATOR_NAME;
    }
}
