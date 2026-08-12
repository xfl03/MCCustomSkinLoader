package customskinloader.bootstrap;

public final class ModLoaderInfo {
    public static final String NAME_PROPERTY = "customskinloader.modLoader.name";
    public static final String VERSION_PROPERTY = "customskinloader.modLoader.version";

    private ModLoaderInfo() {
    }

    public static void publish(String name, String version) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Mod loader name must not be empty");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Mod loader version must not be empty");
        }

        System.setProperty(NAME_PROPERTY, name);
        System.setProperty(VERSION_PROPERTY, version);
        BootstrapLogger.LOGGER.info("Detected mod loader " + name + " " + version);
    }
}
