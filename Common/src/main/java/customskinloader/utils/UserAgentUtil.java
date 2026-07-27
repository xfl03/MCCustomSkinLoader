package customskinloader.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import customskinloader.CustomSkinLoader;

public final class UserAgentUtil {
    public static final String DEFAULT_USER_AGENT_PLACEHOLDER = "{DEFAULT_USER_AGENT}";
    public static final String CSL_VERSION_PLACEHOLDER = "{CSL_VERSION}";
    public static final String MINECRAFT_VERSION_PLACEHOLDER = "{MINECRAFT_VERSION}";
    public static final String MOD_LOADER_NAME_PLACEHOLDER = "{MOD_LOADER_NAME}";
    public static final String MOD_LOADER_VERSION_PLACEHOLDER = "{MOD_LOADER_VERSION}";
    public static final String JAVA_VERSION_PLACEHOLDER = "{JAVA_VERSION}";

    private static final String UNKNOWN = "unknown";
    private static volatile RuntimeInfo runtimeInfo;

    private UserAgentUtil() {
    }

    public static String getUserAgent() {
        return expandUserAgent(null);
    }

    public static String expandUserAgent(String userAgent) {
        RuntimeInfo info = getRuntimeInfo();
        return expandUserAgent(userAgent, info.modVersion, info.minecraftVersion, info.loader.name, info.loader.version, info.javaVersion);
    }

    static String buildUserAgent(String modVersion, String minecraftVersion, String loaderName, String loaderVersion, String javaVersion) {
        return "CustomSkinLoader/" + sanitizeComponent(modVersion)
                + " (Minecraft/" + sanitizeComponent(minecraftVersion)
                + "; " + sanitizeComponent(loaderName) + "/" + sanitizeComponent(loaderVersion)
                + "; Java/" + sanitizeComponent(javaVersion) + ")";
    }

    static String expandUserAgent(String userAgent, String modVersion, String minecraftVersion, String loaderName, String loaderVersion, String javaVersion) {
        String defaultUserAgent = buildUserAgent(modVersion, minecraftVersion, loaderName, loaderVersion, javaVersion);
        if (userAgent == null) {
            return defaultUserAgent;
        }
        return userAgent
                .replace(DEFAULT_USER_AGENT_PLACEHOLDER, defaultUserAgent)
                .replace(CSL_VERSION_PLACEHOLDER, sanitizeComponent(modVersion))
                .replace(MINECRAFT_VERSION_PLACEHOLDER, sanitizeComponent(minecraftVersion))
                .replace(MOD_LOADER_NAME_PLACEHOLDER, sanitizeComponent(loaderName))
                .replace(MOD_LOADER_VERSION_PLACEHOLDER, sanitizeComponent(loaderVersion))
                .replace(JAVA_VERSION_PLACEHOLDER, sanitizeComponent(javaVersion));
    }

    private static RuntimeInfo getRuntimeInfo() {
        RuntimeInfo info = runtimeInfo;
        if (info != null) {
            return info;
        }

        LoaderInfo loader = detectLoader();
        info = new RuntimeInfo(
                CustomSkinLoader.CustomSkinLoader_FULL_VERSION,
                MinecraftUtil.getMinecraftMainVersion(),
                loader,
                System.getProperty("java.version")
        );
        if (loader.isKnown()) {
            runtimeInfo = info;
        }
        return info;
    }

    private static LoaderInfo detectLoader() {
        LoaderInfo loader = detectModListLoader("NeoForge", "net.neoforged.fml.ModList", "neoforge");
        if (loader != null) {
            String version = loader.version;
            if (UNKNOWN.equals(version)) {
                version = firstNonEmpty(
                        invokeStaticString("net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion", "getVersion"),
                        invokeVersionInfo("net.neoforged.fml.loading.FMLLoader", "neoForgeVersion")
                );
            }
            return new LoaderInfo(loader.name, version);
        }

        loader = detectModListLoader("Forge", "net.minecraftforge.fml.ModList", "forge");
        if (loader != null) {
            String version = loader.version;
            if (UNKNOWN.equals(version)) {
                version = firstNonEmpty(
                        invokeStaticString("net.minecraftforge.versions.forge.ForgeVersion", "getVersion"),
                        invokeVersionInfo("net.minecraftforge.fml.loading.FMLLoader", "forgeVersion")
                );
            }
            return new LoaderInfo(loader.name, version);
        }

        Class<?> legacyForge = findClass("net.minecraftforge.common.ForgeVersion");
        if (legacyForge != null) {
            return new LoaderInfo("Forge", valueOrUnknown(invokeStaticString(legacyForge, "getVersion")));
        }

        loader = detectQuilt();
        if (loader != null) {
            return loader;
        }

        loader = detectFabric();
        if (loader != null) {
            return loader;
        }

        return new LoaderInfo("Unknown", UNKNOWN);
    }

    private static LoaderInfo detectQuilt() {
        Class<?> quiltLoader = findClass("org.quiltmc.loader.api.QuiltLoader");
        if (quiltLoader == null) {
            return null;
        }

        String version = findModVersion(invokeStatic(quiltLoader, "getModContainer", String.class, "quilt_loader"));
        return new LoaderInfo("Quilt", firstNonEmpty(version, getImplementationVersion(quiltLoader)));
    }

    private static LoaderInfo detectFabric() {
        Class<?> fabricLoader = findClass("net.fabricmc.loader.api.FabricLoader");
        if (fabricLoader == null) {
            return null;
        }

        Object loader = invokeStatic(fabricLoader, "getInstance");
        String version = findModVersion(invoke(loader, "getModContainer", String.class, "fabricloader"));
        return new LoaderInfo("Fabric", firstNonEmpty(
                version,
                readStaticString("net.fabricmc.loader.impl.FabricLoaderImpl", "VERSION"),
                getImplementationVersion(fabricLoader)
        ));
    }

    private static LoaderInfo detectModListLoader(String name, String modListClassName, String modId) {
        Class<?> modListClass = findClass(modListClassName);
        if (modListClass == null) {
            return null;
        }

        Object modList = invokeStatic(modListClass, "get");
        Object container = unwrapOptional(invoke(modList, "getModContainerById", String.class, modId));
        Object modInfo = invoke(container, "getModInfo");
        Object version = invoke(modInfo, "getVersion");
        return new LoaderInfo(name, valueOrUnknown(version == null ? null : version.toString()));
    }

    private static String findModVersion(Object containerValue) {
        Object container = unwrapOptional(containerValue);
        Object metadata = invokeFirst(container, "metadata", "getMetadata");
        Object version = invokeFirst(metadata, "version", "getVersion");
        Object versionText = invokeFirst(version, "raw", "getFriendlyString");
        return versionText == null ? null : versionText.toString();
    }

    private static String invokeVersionInfo(String className, String versionMethod) {
        Class<?> loaderClass = findClass(className);
        Object versionInfo = invokeStatic(loaderClass, "versionInfo");
        Object version = invoke(versionInfo, versionMethod);
        return version == null ? null : version.toString();
    }

    private static String invokeStaticString(String className, String methodName) {
        return invokeStaticString(findClass(className), methodName);
    }

    private static String invokeStaticString(Class<?> type, String methodName) {
        Object value = invokeStatic(type, methodName);
        return value == null ? null : value.toString();
    }

    private static Object invokeFirst(Object target, String... methodNames) {
        for (String methodName : methodNames) {
            Object value = invoke(target, methodName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Object invokeStatic(Class<?> type, String methodName) {
        return invokeMethod(type, null, methodName, null, null);
    }

    private static Object invokeStatic(Class<?> type, String methodName, Class<?> parameterType, Object argument) {
        return invokeMethod(type, null, methodName, parameterType, argument);
    }

    private static Object invoke(Object target, String methodName) {
        return target == null ? null : invokeMethod(target.getClass(), target, methodName, null, null);
    }

    private static Object invoke(Object target, String methodName, Class<?> parameterType, Object argument) {
        return target == null ? null : invokeMethod(target.getClass(), target, methodName, parameterType, argument);
    }

    private static Object invokeMethod(Class<?> type, Object target, String methodName, Class<?> parameterType, Object argument) {
        if (type == null) {
            return null;
        }

        try {
            Method method = parameterType == null ? type.getMethod(methodName) : type.getMethod(methodName, parameterType);
            if (target == null && !Modifier.isStatic(method.getModifiers())) {
                return null;
            }
            return parameterType == null ? method.invoke(target) : method.invoke(target, argument);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static Object unwrapOptional(Object value) {
        if (value instanceof Optional) {
            return ((Optional<?>) value).orElse(null);
        }
        return value;
    }

    private static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private static String getImplementationVersion(Class<?> type) {
        Package loaderPackage = type.getPackage();
        return loaderPackage == null ? null : loaderPackage.getImplementationVersion();
    }

    private static String readStaticString(String className, String fieldName) {
        return readStaticString(findClass(className), fieldName);
    }

    static String readStaticString(Class<?> type, String fieldName) {
        if (type == null) {
            return null;
        }

        try {
            Field field = type.getField(fieldName);
            if (!Modifier.isStatic(field.getModifiers())) {
                return null;
            }
            Object value = field.get(null);
            return value == null ? null : value.toString();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return UNKNOWN;
    }

    private static String valueOrUnknown(String value) {
        return firstNonEmpty(value);
    }

    private static String sanitizeComponent(String value) {
        String component = valueOrUnknown(value).trim();
        StringBuilder sanitized = new StringBuilder(component.length());
        for (int i = 0; i < component.length(); i++) {
            char c = component.charAt(i);
            sanitized.append(isTokenCharacter(c) ? c : '_');
        }
        return sanitized.toString();
    }

    private static boolean isTokenCharacter(char c) {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || "!#$%&'*+-.^_`|~".indexOf(c) >= 0;
    }

    private static final class LoaderInfo {
        private final String name;
        private final String version;

        private LoaderInfo(String name, String version) {
            this.name = name;
            this.version = valueOrUnknown(version);
        }

        private boolean isKnown() {
            return !"Unknown".equals(name) && !UNKNOWN.equals(version);
        }
    }

    private static final class RuntimeInfo {
        private final String modVersion;
        private final String minecraftVersion;
        private final LoaderInfo loader;
        private final String javaVersion;

        private RuntimeInfo(String modVersion, String minecraftVersion, LoaderInfo loader, String javaVersion) {
            this.modVersion = modVersion;
            this.minecraftVersion = minecraftVersion;
            this.loader = loader;
            this.javaVersion = javaVersion;
        }
    }
}
