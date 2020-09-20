package customskinloader.fabric;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import customskinloader.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    public static Logger logger = new Logger(new File("./CustomSkinLoader/FabricPlugin.log"));

    private MinecraftVersion version = null;

    @Override
    public void onLoad(String mixinPackage) {
        URL versionJson = this.getClass().getResource("/version.json");
        if (versionJson != null) {
            logger.info("\"version.json\": " + versionJson.toString());
            try (
                InputStream is = versionJson.openStream();
                InputStreamReader isr = new InputStreamReader(is)
            ) {
                this.version = new Gson().fromJson(isr, MinecraftVersion.class);
                logger.info(this.version.toString());
            } catch (Throwable t) {
                logger.warning("An exception occurred when reading \"version.json\"!");
                logger.warning(t);
            }
        } else {
            logger.warning("Can't read \"version.json\"! Ignore this message if the version you start is earlier than 18w47b.");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean result = true;
        if (mixinClassName.endsWith(".MixinThreadDownloadImageData")) {
            result = this.version != null && this.version.world_version >= 2205 && this.version.protocol_version >= 554; // 19w38a+
        } else if (mixinClassName.endsWith(".MixinLayerCape") || mixinClassName.endsWith(".MixinRenderPlayer")) {
            result = this.version != null && this.version.world_version >= 2210 && this.version.protocol_version >= 558; // 19w41a+
        }
        logger.info("target: " + targetClassName + ", mixin: " + mixinClassName + ", result: " + result);
        return result;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    // To be compatible with 0.7.11
    public void preApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    // To be compatible with 0.7.11
    public void postApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
