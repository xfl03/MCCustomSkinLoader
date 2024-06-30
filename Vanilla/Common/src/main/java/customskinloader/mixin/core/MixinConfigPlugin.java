package customskinloader.mixin.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import customskinloader.log.LogManager;
import customskinloader.log.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    public static Logger logger = LogManager.getLogger("Core");
    private long world_version;
    private long protocol_version;

    @Override
    public void onLoad(String mixinPackage) {
        LogManager.setLogFile(Paths.get("./CustomSkinLoader/CustomSkinLoader.log"));
        URL versionJson = ClassLoader.getSystemClassLoader().getResource("version.json");
        if (versionJson != null) {
            logger.info("\"version.json\": " + versionJson);
            try (
                InputStream is = versionJson.openStream();
                InputStreamReader isr = new InputStreamReader(is)
            ) {
                JsonObject object = new JsonParser().parse(isr).getAsJsonObject();
                String name = object.get("name").getAsString();
                this.world_version = object.get("world_version").getAsLong();
                this.protocol_version = object.get("protocol_version").getAsLong();
                logger.info("MinecraftVersion: {name='" + name + "', world_version='" + this.world_version + "', protocol_version='" + this.protocol_version + "'}");
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
        if (mixinClassName.endsWith(".MixinIImageBuffer")) {
            result = this.world_version <= 2204 && this.protocol_version <= 553; // 18w43b ~ 19w37a
        } else if (mixinClassName.endsWith(".MixinLayerCape") || mixinClassName.endsWith(".MixinRenderPlayer")) {
            result = this.world_version >= 2210 && this.protocol_version >= 558; // 19w41a+
        } else if (mixinClassName.endsWith(".MixinSkinManager$V1")) {
            result = this.world_version <= 3465 && (this.protocol_version <= 763 || (this.protocol_version >= 801 && this.protocol_version <= 803) || (this.protocol_version >= 0x40000001 && this.protocol_version <= 0x4000008E)); // 18w43b ~ 1.20.1
        } else if (mixinClassName.endsWith(".MixinSkinManager$V2") || mixinClassName.endsWith(".MixinSkinManager$1") || mixinClassName.endsWith(".MixinSkinManager$CacheKey") || mixinClassName.endsWith(".MixinSkinManager$TextureCache")) {
            result = this.world_version >= 3567 &&  ((this.protocol_version >= 764 && this.protocol_version < 801) || (this.protocol_version > 803 && this.protocol_version < 0x40000001) || this.protocol_version >= 0x40000090); // 23w31a+
        } else if (mixinClassName.endsWith(".MixinSkinManager$V3")) {
            result = this.world_version >= 3684 &&  ((this.protocol_version >= 765 && this.protocol_version < 801) || (this.protocol_version > 803 && this.protocol_version < 0x40000001) || this.protocol_version >= 0x4000009D); // 23w42a+
        } else if (mixinClassName.endsWith(".MixinThreadDownloadImageData$V1")) {
            result = (this.world_version >= 2205 && this.world_version <= 2722) && ((this.protocol_version >= 554 && this.protocol_version <= 754) || (this.protocol_version >= 801 && this.protocol_version <= 803) || (this.protocol_version >= 0x40000001 && this.protocol_version <= 0x40000022)); // 19w38a ~ 1.17-rc1
        } else if (mixinClassName.endsWith(".MixinThreadDownloadImageData$V2")) {
            result = this.world_version >= 2723 && ((this.protocol_version >= 755 && this.protocol_version < 801) || (this.protocol_version > 803 && this.protocol_version < 0x40000001) || this.protocol_version >= 0x40000023); // 1.17-rc2+
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

