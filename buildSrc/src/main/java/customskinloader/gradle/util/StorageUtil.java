package customskinloader.gradle.util;

public class StorageUtil {
    public static String getKey(String filename) {
        String name = filename.substring(0, filename.lastIndexOf('.'));
        if (name.indexOf('-') == -1) {
            return null;
        }
        if (filename.endsWith(".jar")) {
            if (filename.contains("Fabric") || filename.contains("Forge")) {
                return String.format(
                        "mods/%s",
                        filename
                );
            }
        }
        return filename;
    }
}
