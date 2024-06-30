package customskinloader.gradle.util;

import org.gradle.api.Project;
import org.gradle.util.VersionNumber;

import java.util.*;
import java.util.stream.Collectors;

public class VersionUtil {
    public static boolean isRelease(Project rootProject) {
        String s = System.getenv("IS_SNAPSHOT");
        // When we cannot find environment variable, it means we are in development, so it is not a release
        if (s == null) {
            return false;
        }
        return s.equals("0") || s.equals("false");
    }

    public static boolean isSnapshot(Project rootProject) {
        return !isRelease(rootProject);
    }

    public static String getBuildNum() {
        if (System.getenv("GITHUB_RUN_NUMBER") != null) {
            return System.getenv("GITHUB_RUN_NUMBER");
        }
        if (System.getenv("CIRCLE_BUILD_NUM") != null) {
            return System.getenv("CIRCLE_BUILD_NUM");
        }
        return "00";
    }

    //Example: 14.10a-SNAPSHOT-33
    public static String getCSLVersion(Project rootProject) {

        return ConfigUtil.getConfigString(rootProject, "version")  +
                (isRelease(rootProject) ? "" : ("-SNAPSHOT-" + getBuildNum()));
    }

    //Example: 14.10a-s33
    public static String getShortVersion(Project rootProject) {
        return getCSLVersion(rootProject).replace("SNAPSHOT-", "s");
    }

    public static String getMcVersion(String filename) {
        return filename.substring(filename.indexOf('_') + 1, filename.indexOf('-'));
    }

    public static Collection<String> getMcMajorVersions(String version) {
        if (version == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(version.split(","))
                .map(VersionNumber::parse)
                .map(it -> String.format("%s.%s", it.getMajor(), it.getMinor()))
                .collect(Collectors.toSet());
    }

    public static String getEdition(Project project) {
        return project.getName().replace("/", "");
    }

    public static Map<String, String> parseDependencies(String dependencies) {
        Map<String, String> map = new LinkedHashMap<>();
        if (dependencies != null) {
            String[] loaderVersions = dependencies.split(";");
            for (String loader : loaderVersions) {
                String[] mcVersions = loader.split("\\|");
                map.put(mcVersions[0], mcVersions[1]);
            }
        }
        return map;
    }

    public static String getDependencies(String dependencies) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> dependency : parseDependencies(dependencies).entrySet()) {
            sb.append("\"").append(dependency.getKey()).append("\": \"").append(dependency.getValue()).append("\",\n    ");
        }
        return sb.substring(1, sb.length() - 7);
    }
}
