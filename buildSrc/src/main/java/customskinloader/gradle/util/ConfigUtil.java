package customskinloader.gradle.util;

import groovy.util.ConfigObject;
import org.gradle.api.Project;

public class ConfigUtil {
    public static ConfigObject getConfig(Project project) {
        if (!project.getExtensions().getExtraProperties().has("config")) {
            System.out.printf("Config not found in '%s'", project.getName());
            return new ConfigObject();
        }
        Object o = project.getExtensions().getExtraProperties().get("config");
        if (!(o instanceof ConfigObject)) {
            System.out.printf("Config in '%s' is not ConfigObject", project.getName());
            return new ConfigObject();
        }
        return (ConfigObject) o;
    }

    public static String getConfigString(Project project, String key) {
        ConfigObject config = getConfig(project);
        Object o = config.get(key);
        if (o == null) {
            //System.out.printf("Config '%s' not found in '%s'", key, project.getName());
            return null;
        }
        if (!(o instanceof String)) {
            System.out.printf("Config '%s' in '%s' is not String", key, project.getName());
            return null;
        }
        return (String) config.getProperty(key);
    }
}
