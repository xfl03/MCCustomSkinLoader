package customskinloader.gradle.util;

import org.gradle.api.Project;
import org.gradle.util.VersionNumber;

import java.util.*;
import java.util.stream.Collectors;

public class VersionUtil {
    public static boolean isRelease(Project rootProject) {
        String s = ConfigUtil.getConfigString(rootProject, "snapshot");
        return s == null || s.equals("") || s.equals("0");
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

        return ConfigUtil.getConfigString(rootProject, "version") +
                ConfigUtil.getConfigString(rootProject, "dev_version") +
                (isRelease(rootProject) ? "" : ("-SNAPSHOT-" + getBuildNum()));
    }

    //Example: 14.10a-s33
    public static String getShortVersion(Project rootProject) {
        return getCSLVersion(rootProject).replace("SNAPSHOT-", "s");
    }

    public static String getMcVersion(String filename) {
        if (filename.endsWith(".json")) {
            return filename.substring(0, filename.indexOf('-'));
        }
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
        return project.getName().contains("Vanilla") ?
                ConfigUtil.getConfigString(project, "minecraft_version") :
                project.getName().replace("/", "");
    }

    public static List<String> getLoaders(Project project) {
        String loaderName = project.getName().split("/")[0];
        List<String> loaders = new ArrayList<>(2);
        loaders.add(loaderName);
        if (loaderName.equals("Fabric")) {
            loaders.add("Quilt");
        }
        return loaders;
    }
}
