package customskinloader.bootstrap.fabric.v1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.ModLoaderInfo;
import customskinloader.bootstrap.installer.CommonJarInstaller;
import customskinloader.bootstrap.transformer.ClassTransformationReport;
import customskinloader.bootstrap.transformer.TransformerBootstrapSupport;
import customskinloader.bootstrap.util.ReflectionUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.Config;

final class TransformerBootstrap {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;
    private static final TransformerBootstrapSupport SUPPORT = new TransformerBootstrapSupport(TransformerBootstrap.class, "intermediary");
    private static final String DUMMY_MIXIN = "customskinloader.bootstrap.fabric.v1.mixin.DummyMixin";

    private TransformerBootstrap() {
    }

    public static void initialize() {
        LOGGER.info("Initializing CustomSkinLoader Bootstrap for Fabric");
        ModLoaderInfo.publish("Fabric", FabricLoader.getInstance().getModContainer("fabricloader").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("unknown"));

        try {
            Path commonJar = CommonJarInstaller.releaseCommonJar(FabricLoader.getInstance().getGameDir(), "intermediary");
            FabricLauncherBase.getLauncher().addToClassPath(commonJar, "customskinloader.");
            LOGGER.info("Added CustomSkinLoader Common jar to Fabric class path: " + BootstrapLogger.formatPath(commonJar));
        } catch (Exception exception) {
            LOGGER.error("Failed to prepare CustomSkinLoader runtime artifacts for Fabric", exception);
            throw new IllegalStateException("Failed to prepare CustomSkinLoader runtime artifacts for Fabric", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static void applyMixinTargets(String mixinPackage) {
        try {
            IMixinConfig mixinConfig = null;
            Field allConfigsField = ReflectionUtils.findField(Config.class, "allConfigs");
            for (Object configHandle : ((Map<?, ?>) allConfigsField.get(null)).values()) {
                IMixinConfig mixinConfig0 = ((Config) configHandle).getConfig();
                if (mixinPackage.equals(mixinConfig0.getMixinPackage())) {
                    mixinConfig = mixinConfig0;
                    break;
                }
            }
            if (mixinConfig == null) {
                throw new IllegalStateException("Could not find the owning MixinConfig for " + mixinPackage);
            }

            int appliedTargetCount = 0;
            Class<?> mixinConfigClass = ReflectionUtils.findClass("org.spongepowered.asm.mixin.transformer.MixinConfig");
            Class<?> mixinInfoClass = ReflectionUtils.findClass("org.spongepowered.asm.mixin.transformer.MixinInfo");

            Field mixinsField = ReflectionUtils.findField(mixinConfigClass, "mixins");
            Field targetClassNamesField =  ReflectionUtils.findField(mixinInfoClass, "targetClassNames");
            Field targetClassesField = ReflectionUtils.findField(mixinInfoClass, "targetClasses");
            Field mixinMappingField = ReflectionUtils.findField(mixinConfigClass, "mixinMapping");
            Field unhandledTargetsField = ReflectionUtils.findField(mixinConfigClass, "unhandledTargets");

            Method addMixinMethod = ReflectionUtils.findMethod(ClassInfo.class, "addMixin", mixinInfoClass);

            for (String targetClassName : SUPPORT.getTargetClassNames()) {
                //applyMixinTarget(mixinConfig, DUMMY_MIXIN, targetClassName);

                boolean applied = false;
                for (Object mixinInfo : (List<?>) mixinsField.get(mixinConfig)) {
                    String currentMixinName = ((IMixinInfo) mixinInfo).getClassName();
                    if (DUMMY_MIXIN.equals(currentMixinName)) {
                        String targetBinaryName = targetClassName.replace('/', '.');
                        String targetInternalName = targetClassName.replace('.', '/');

                        List<String> targetClassNames = (List<String>) targetClassNamesField.get(mixinInfo);
                        if (targetClassNames.contains(targetInternalName)) {
                            applied = true;
                            break;
                        }

                        ClassInfo targetClassInfo = ClassInfo.forName(targetInternalName);
                        if (targetClassInfo == null) {
                            throw new IllegalStateException("Could not resolve target ClassInfo for " + targetInternalName);
                        }

                        addMixinMethod.invoke(targetClassInfo, mixinInfo);
                        List<ClassInfo> targetClasses = (List<ClassInfo>) targetClassesField.get(mixinInfo);
                        targetClasses.add(targetClassInfo);
                        targetClassNames.add(targetInternalName);

                        Map<String, List<Object>> mixinMapping = (Map<String, List<Object>>) mixinMappingField.get(mixinConfig);
                        List<Object> mappedMixins = mixinMapping.computeIfAbsent(targetBinaryName, k -> new ArrayList<>());
                        if (!mappedMixins.contains(mixinInfo)) {
                            mappedMixins.add(mixinInfo);
                        }

                        ((Set<String>) unhandledTargetsField.get(mixinConfig)).add(targetBinaryName);
                        applied = true;
                        break;
                    }
                }

                if (!applied) {
                    throw new IllegalStateException("Could not find mixin " + DUMMY_MIXIN + " in the current config");
                }

                LOGGER.debug("Applied Fabric Mixin target " + targetClassName);
                appliedTargetCount++;
            }
            LOGGER.info("Applied " + appliedTargetCount + " reflected Fabric Mixin target(s)");
        } catch (ReflectiveOperationException exception) {
            LOGGER.error("Failed to append reflected targets to the current Fabric mixin config", exception);
            throw new IllegalStateException("Failed to append reflected targets to the current Fabric mixin config", exception);
        }
    }

    public static void transformTargetClass(String targetClassName, ClassNode targetClass) {
        ClassTransformationReport report = SUPPORT.transform(targetClassName, targetClass);
        if (report.isModified()) {
            LOGGER.info("Transformed Fabric target " + targetClassName + " with " + report.getAppliedRuleNames());
        }
    }
}
