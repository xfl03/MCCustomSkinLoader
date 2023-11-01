package customskinloader.gradle.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class StorageService {
    private static final List<Storage> storages = new ArrayList<>();

    static {
        storages.add(new CosStorage());
        storages.add(new R2Storage());
    }

    /**
     * Put file to all storage.
     *
     * @param key  key
     * @param file file
     */
    public static void put(String key, Path file) {
        storages.forEach(it -> it.put(key, file));
    }

    /**
     * Put file to all storage.
     *
     * @param key  key
     * @param file file
     */
    public static void put(String key, File file) {
        put(key, file.toPath());
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Put object to all storage.
     *
     * @param key key
     * @param obj obj
     */
    public static void put(String key, Object obj) throws IOException {
        for (Storage it : storages) {
            Path file = Paths.get("build/libs/", key);
            Files.write(file,
                    gson.toJson(obj).replace(BASE_URL, it.getPublicBaseUrl()).getBytes(StandardCharsets.UTF_8));
            it.put(key, file);
        }
    }

    public static final String BASE_URL = "{BASE_URL}";
}
