package customskinloader.bootstrap.neoforge.v2;

import customskinloader.bootstrap.BootstrapLogger;
import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;
import org.apache.logging.log4j.Logger;

public final class ClassProcessorProviderImpl implements ClassProcessorProvider {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    @Override
    public void createProcessors(Context context, Collector collector) {
        // Always register the bridge processor so late transformer registrations can still be observed.
        LOGGER.info("Registering CustomSkinLoader Bootstrap NeoForge class processor");
        collector.add(TransformerBootstrap.createProcessor());
    }
}
