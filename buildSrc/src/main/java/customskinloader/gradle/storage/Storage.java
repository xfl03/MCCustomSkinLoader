package customskinloader.gradle.storage;

import java.nio.file.Path;

public interface Storage {
    /**
     * Put file to object storage service.
     * @param key key
     * @param file file
     */
    void put(String key, Path file);

    String getPublicBaseUrl();
}
