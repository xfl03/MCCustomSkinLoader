package customskinloader.gradle.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CslLatest {
    public String version;
    public Map<String, String> downloads = new LinkedHashMap<>();
    public Map<String, String> launchermeta = new LinkedHashMap<>();

    public CslLatest(String version) {
        this.version = version;
    }

    public String getUrl(String edition) {
        return Optional.ofNullable(downloads.get(edition))
                .orElseGet(() -> launchermeta.get(edition));
    }
}
