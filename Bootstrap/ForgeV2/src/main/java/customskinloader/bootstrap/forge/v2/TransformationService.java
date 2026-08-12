package customskinloader.bootstrap.forge.v2;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import customskinloader.bootstrap.BootstrapLogger;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import org.apache.logging.log4j.Logger;

public final class TransformationService implements ITransformationService {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    @Override
    public String name() {
        return "customskinloader-bootstrap";
    }

    @Override
    public void initialize(IEnvironment environment) {
        LOGGER.info("Initializing CustomSkinLoader Bootstrap transformation service");
        TransformerBootstrap.publishModLoaderInfo();
        try {
            List<Path> extraLocators = ModDirTransformerDiscoverer.getExtraLocators();
            Path servicePath = Paths.get(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            if (!extraLocators.contains(servicePath)) {
                extraLocators.add(servicePath);
                LOGGER.info("Registered CustomSkinLoader Bootstrap service path as Forge extra locator: " + BootstrapLogger.formatPath(servicePath));
            }
        } catch (NoSuchMethodError e) { // 1.13.2-25.0.216 ~ 1.14.4-28.1.64
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            LOGGER.info("Using context ClassLoader fallback for legacy Forge transformation discovery");
        } catch (NoClassDefFoundError ignored) { // NeoForge
            LOGGER.debug("Skipping Forge ModDirTransformerDiscoverer setup on NeoForge");
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to resolve CustomSkinLoader bootstrap service path", e);
            throw new IllegalStateException("Unable to resolve CustomSkinLoader bootstrap service path", e);
        }
    }

    @Override
    public void beginScanning(IEnvironment environment) {

    }

    @Override
    public void onLoad(IEnvironment environment, Set<String> otherServices) {
        LOGGER.info("Loaded CustomSkinLoader Bootstrap transformation service with " + otherServices.size() + " other service(s)");
    }

    @Override
    public List<ITransformer> transformers() {
        List<ITransformer> transformers = TransformerBootstrap.buildTransformers();
        LOGGER.info("Provided " + transformers.size() + " CustomSkinLoader ModLauncher transformer(s)");
        return transformers;
    }
}
