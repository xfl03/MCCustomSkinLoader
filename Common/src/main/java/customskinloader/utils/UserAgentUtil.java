package customskinloader.utils;

import customskinloader.CustomSkinLoader;

public final class UserAgentUtil {
    public static final String DEFAULT_USER_AGENT_PLACEHOLDER = "{DEFAULT_USER_AGENT}";
    public static final String CSL_VERSION_PLACEHOLDER = "{CSL_VERSION}";
    public static final String MINECRAFT_VERSION_PLACEHOLDER = "{MINECRAFT_VERSION}";
    public static final String MOD_LOADER_NAME_PLACEHOLDER = "{MOD_LOADER_NAME}";
    public static final String MOD_LOADER_VERSION_PLACEHOLDER = "{MOD_LOADER_VERSION}";
    public static final String JAVA_VERSION_PLACEHOLDER = "{JAVA_VERSION}";

    private UserAgentUtil() {
    }

    public static String expandUserAgent(String userAgent) {
        String modVersion = sanitizeComponent(CustomSkinLoader.CustomSkinLoader_FULL_VERSION);
        String minecraftVersion = sanitizeComponent(MinecraftUtil.getMinecraftMainVersion());
        String loaderName = sanitizeComponent(MinecraftUtil.getLoaderName());
        String loaderVersion = sanitizeComponent(MinecraftUtil.getLoaderVersion());
        String javaVersion = sanitizeComponent(System.getProperty("java.version", "unknown"));

        String defaultUserAgent = "CustomSkinLoader/" + modVersion
                + " (Minecraft/" + minecraftVersion
                + "; " + loaderName + "/" + loaderVersion
                + "; Java/" + javaVersion + ")";

        if (userAgent == null) {
            return defaultUserAgent;
        }
        return userAgent
                .replace(DEFAULT_USER_AGENT_PLACEHOLDER, defaultUserAgent)
                .replace(CSL_VERSION_PLACEHOLDER, modVersion)
                .replace(MINECRAFT_VERSION_PLACEHOLDER, minecraftVersion)
                .replace(MOD_LOADER_NAME_PLACEHOLDER, loaderName)
                .replace(MOD_LOADER_VERSION_PLACEHOLDER, loaderVersion)
                .replace(JAVA_VERSION_PLACEHOLDER, javaVersion);
    }

    private static String sanitizeComponent(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "unknown";
        }
        String component = value.trim();
        StringBuilder sanitized = new StringBuilder(component.length());
        for (int i = 0; i < component.length(); i++) {
            char c = component.charAt(i);
            sanitized.append(c >= '0' && c <= '9'
                    || c >= 'A' && c <= 'Z'
                    || c >= 'a' && c <= 'z'
                    || "!#$%&'*+-.^_`|~".indexOf(c) >= 0 ? c : '_');
        }
        return sanitized.toString();
    }
}
