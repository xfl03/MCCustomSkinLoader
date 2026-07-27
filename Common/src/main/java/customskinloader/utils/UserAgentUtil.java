package customskinloader.utils;

import customskinloader.CustomSkinLoader;

public final class UserAgentUtil {
    public static final String DEFAULT_USER_AGENT_PLACEHOLDER = "{DEFAULT_USER_AGENT}";
    public static final String CSL_VERSION_PLACEHOLDER = "{CSL_VERSION}";
    public static final String MINECRAFT_VERSION_PLACEHOLDER = "{MINECRAFT_VERSION}";
    public static final String MOD_LOADER_NAME_PLACEHOLDER = "{MOD_LOADER_NAME}";
    public static final String MOD_LOADER_VERSION_PLACEHOLDER = "{MOD_LOADER_VERSION}";
    public static final String JAVA_VERSION_PLACEHOLDER = "{JAVA_VERSION}";

    private static final String MOD_LOADER_NAME_PROPERTY = "customskinloader.modLoader.name";
    private static final String MOD_LOADER_VERSION_PROPERTY = "customskinloader.modLoader.version";
    private static volatile RuntimeInfo runtimeInfo;

    private UserAgentUtil() {
    }

    public static String getUserAgent() {
        return expandUserAgent(null);
    }

    public static String expandUserAgent(String userAgent) {
        RuntimeInfo info = getRuntimeInfo();
        return expandUserAgent(userAgent, info.modVersion, info.minecraftVersion, info.loaderName, info.loaderVersion, info.javaVersion);
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

        info = new RuntimeInfo(
                CustomSkinLoader.CustomSkinLoader_FULL_VERSION,
                MinecraftUtil.getMinecraftMainVersion(),
                requireProperty(MOD_LOADER_NAME_PROPERTY),
                requireProperty(MOD_LOADER_VERSION_PROPERTY),
                requireProperty("java.version")
        );
        runtimeInfo = info;
        return info;
    }

    private static String requireProperty(String propertyName) {
        String value = System.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required Bootstrap property: " + propertyName);
        }
        return value;
    }

    private static String sanitizeComponent(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("User-Agent component must not be empty");
        }
        String component = value.trim();
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

    private static final class RuntimeInfo {
        private final String modVersion;
        private final String minecraftVersion;
        private final String loaderName;
        private final String loaderVersion;
        private final String javaVersion;

        private RuntimeInfo(String modVersion, String minecraftVersion, String loaderName, String loaderVersion, String javaVersion) {
            this.modVersion = modVersion;
            this.minecraftVersion = minecraftVersion;
            this.loaderName = loaderName;
            this.loaderVersion = loaderVersion;
            this.javaVersion = javaVersion;
        }
    }
}
