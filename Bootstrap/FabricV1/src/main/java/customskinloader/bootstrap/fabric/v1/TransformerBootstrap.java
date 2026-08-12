package customskinloader.bootstrap.fabric.v1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.ModLoaderInfo;
import customskinloader.bootstrap.installer.CommonJarInstaller;
import customskinloader.bootstrap.transformer.ClassTransformationReport;
import customskinloader.bootstrap.transformer.TransformerBootstrapSupport;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.Config;

final class TransformerBootstrap {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;
    private static final TransformerBootstrapSupport SUPPORT = new TransformerBootstrapSupport(TransformerBootstrap.class, "intermediary");
    private static final Object LOCK = new Object();
    private static final List<PendingMixinTarget> PENDING_MIXIN_TARGETS = new ArrayList<>();
    private static final String DUMMY_MIXIN = "customskinloader.bootstrap.fabric.v1.mixin.DummyMixin";

    private TransformerBootstrap() {
    }

    public static void initialize() {
        LOGGER.info("Initializing CustomSkinLoader Bootstrap for Fabric");
        publishModLoaderInfo();
        releaseRuntimeArtifacts();
        int targetCount = 0;
        for (String targetClassName : SUPPORT.collectTargetClassNames()) {
            registerMixinTarget(DUMMY_MIXIN, targetClassName.replace('/', '.'));
            targetCount++;
        }
        LOGGER.info("Initialized CustomSkinLoader Bootstrap for Fabric with " + targetCount + " reflected Mixin target(s)");
    }

    private static void publishModLoaderInfo() {
        ModLoaderInfo.publish("Fabric", FabricLoader.getInstance().getModContainer("fabricloader").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("unknown"));
    }

    private static void releaseRuntimeArtifacts() {
        try {
            Path commonJar = CommonJarInstaller.releaseCommonJar(FabricLoader.getInstance().getGameDir(), "intermediary");
            FabricLauncherBase.getLauncher().addToClassPath(commonJar, "customskinloader.");
            LOGGER.info("Added CustomSkinLoader Common jar to Fabric class path: " + BootstrapLogger.formatPath(commonJar));
        } catch (Exception exception) {
            LOGGER.error("Failed to prepare CustomSkinLoader runtime artifacts for Fabric", exception);
            throw new IllegalStateException("Failed to prepare CustomSkinLoader runtime artifacts for Fabric", exception);
        }
    }

    public static void registerMixinTarget(String mixinClassName, String targetClassName) {
        synchronized (LOCK) {
            PENDING_MIXIN_TARGETS.add(new PendingMixinTarget(mixinClassName, targetClassName));
            LOGGER.debug("Queued Fabric Mixin target " + targetClassName + " for " + mixinClassName);
        }
    }

    public static void applyPendingMixinTargets(IMixinConfigPlugin plugin) {
        synchronized (LOCK) {
            if (PENDING_MIXIN_TARGETS.isEmpty()) {
                return;
            }

            try {
                IMixinConfig mixinConfig = findOwningMixinConfig(plugin);
                int appliedTargetCount = 0;
                Iterator<PendingMixinTarget> iterator = PENDING_MIXIN_TARGETS.iterator();
                while (iterator.hasNext()) {
                    PendingMixinTarget pendingTarget = iterator.next();
                    applyMixinTarget(mixinConfig, pendingTarget.mixinClassName, pendingTarget.targetClassName);
                    iterator.remove();
                    appliedTargetCount++;
                }
                LOGGER.info("Applied " + appliedTargetCount + " reflected Fabric Mixin target(s)");
            } catch (ReflectiveOperationException exception) {
                LOGGER.error("Failed to append reflected targets to the current Fabric mixin config", exception);
                throw new IllegalStateException("Failed to append reflected targets to the current Fabric mixin config", exception);
            }
        }
    }

    public static void transformTargetClass(String targetClassName, ClassNode targetClass) {
        ClassTransformationReport report = SUPPORT.transformClassNode(targetClassName, targetClass);
        ClassNode transformedNode = report.getTransformedClassNode();
        if (transformedNode != targetClass) {
            TransformerBootstrapSupport.copyClassNode(transformedNode, targetClass);
        }
        if (report.isModified()) {
            LOGGER.info("Transformed Fabric target " + targetClassName + " with " + report.getAppliedTransformerNames());
        }
    }

    private static IMixinConfig findOwningMixinConfig(IMixinConfigPlugin plugin) throws ReflectiveOperationException {
        Field allConfigsField = Config.class.getDeclaredField("allConfigs");
        allConfigsField.setAccessible(true);

        for (Object configHandle : ((Map<?, ?>) allConfigsField.get(null)).values()) {
            IMixinConfig mixinConfig = ((Config) configHandle).getConfig();
            if (mixinConfig.getPlugin() == plugin) {
                return mixinConfig;
            }
        }

        throw new IllegalStateException("Could not find the owning MixinConfig for " + plugin.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    private static void applyMixinTarget(IMixinConfig mixinConfig, String mixinClassName, String targetClassName) throws ReflectiveOperationException {
        IMixinInfo mixinInfo = findMixinInfo(mixinConfig, mixinClassName);
        String targetBinaryName = targetClassName.replace('/', '.');
        String targetInternalName = targetClassName.replace('.', '/');

        List<String> targetClassNames = (List<String>) getField(mixinInfo, "targetClassNames");
        if (targetClassNames.contains(targetInternalName)) {
            return;
        }

        ClassInfo targetClassInfo = ClassInfo.forName(targetInternalName);
        if (targetClassInfo == null) {
            throw new IllegalStateException("Could not resolve target ClassInfo for " + targetInternalName);
        }

        Method addMixin = ClassInfo.class.getDeclaredMethod("addMixin", Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo"));
        addMixin.setAccessible(true);
        addMixin.invoke(targetClassInfo, mixinInfo);

        List<ClassInfo> targetClasses = (List<ClassInfo>) getField(mixinInfo, "targetClasses");
        targetClasses.add(targetClassInfo);
        targetClassNames.add(targetInternalName);

        Map<String, List<Object>> mixinMapping = (Map<String, List<Object>>) getField(mixinConfig, "mixinMapping");
        List<Object> mappedMixins = mixinMapping.computeIfAbsent(targetBinaryName, k -> new ArrayList<>());
        if (!mappedMixins.contains(mixinInfo)) {
            mappedMixins.add(mixinInfo);
        }

        ((Set<String>) getField(mixinConfig, "unhandledTargets")).add(targetBinaryName);
    }

    @SuppressWarnings("unchecked")
    private static IMixinInfo findMixinInfo(IMixinConfig mixinConfig, String mixinClassName) throws ReflectiveOperationException {
        for (IMixinInfo mixinInfo : (List<IMixinInfo>) getField(mixinConfig, "mixins")) {
            String currentMixinName = mixinInfo.getClassName();
            if (mixinClassName.equals(currentMixinName)) {
                return mixinInfo;
            }
        }

        throw new IllegalStateException("Could not find mixin " + mixinClassName + " in the current config");
    }

    private static Object getField(Object instance, String fieldName) throws ReflectiveOperationException {
        Field field = findField(instance.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

    private static Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> currentType = type;
        while (currentType != null) {
            try {
                return currentType.getDeclaredField(fieldName);
            } catch (NoSuchFieldException exception) {
                currentType = currentType.getSuperclass();
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    private static final class PendingMixinTarget {
        private final String mixinClassName;
        private final String targetClassName;

        private PendingMixinTarget(String mixinClassName, String targetClassName) {
            this.mixinClassName = mixinClassName;
            this.targetClassName = targetClassName;
        }
    }
}
