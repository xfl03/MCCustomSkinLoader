package customskinloader.bootstrap.neoforge.v2;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.ModLoaderInfo;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;
import org.apache.logging.log4j.Logger;

// NeoForge 21.9.14+
public final class ClassProcessorProviderImpl implements ClassProcessorProvider {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    @Override
    public void createProcessors(Context context, Collector collector) {
        // Always register the bridge processor so late transformer registrations can still be observed.
        LOGGER.info("Registering CustomSkinLoader Bootstrap NeoForge class processor");
        ModLoaderInfo.publish("NeoForge", FMLLoader.getCurrent().getVersionInfo().neoForgeVersion());
        collector.add(TransformerBootstrap.createProcessor());
    }
}
