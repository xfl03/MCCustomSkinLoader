package customskinloader.gradle.entity;

import org.gradle.util.VersionNumber;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CslDetail {
    public String version;
    public long timestamp;

    Map<String, Map<String, String>> details = new LinkedHashMap<>();

    public CslDetail(String version) {
        this.version = version;
        this.timestamp = System.currentTimeMillis();
    }

    public void addDetail(String mcMajorVersion, String edition, String url) {
        String e = edition
                .replace("ForgeLegacy", "Forge")
                .replace("ForgeActive", "Forge");
        details.computeIfAbsent(mcMajorVersion, (key) -> new LinkedHashMap<>())
                .put(e, url);
    }

    public void sortDetails() {
        details = details.entrySet().stream()
                .sorted(Comparator.comparing(a -> VersionNumber.parse(a.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }
}
