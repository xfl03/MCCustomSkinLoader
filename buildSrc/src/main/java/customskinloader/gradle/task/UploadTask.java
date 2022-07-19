package customskinloader.gradle.task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import customskinloader.gradle.util.CosUtil;
import customskinloader.gradle.util.VersionUtil;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class UploadTask extends DefaultTask {
    public Project rootProject;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private void uploadArtifacts() throws IOException {
        String shortVersion = VersionUtil.getShortVersion(rootProject);
        File dir = rootProject.file("build/libs");
        if (!dir.isDirectory()) {
            return;
        }

        String cslversion = shortVersion.replace(".", "");
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>();
        LinkedHashMap<String, String> map3 = new LinkedHashMap<>();
        map.put("version", shortVersion);
        map.put("downloads", map2);
        map.put("launchermeta", map3);

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String key = CosUtil.getKey(file.getName());
            if (key == null) {
                continue;
            }
            CosUtil.uploadFile(key, file);
            String url = CosUtil.cosUrl + key;
            String mcversion = VersionUtil.getMcVersion(file.getName());
            if (mcversion.equals("ForgeLegacy")) {
                mcversion = "Forge";
            }
            System.out.printf("csl-%s-%s\t%s%n",
                    mcversion.replace(".", "").toLowerCase(), cslversion, url);

            if (key.startsWith("mods/") && key.endsWith(".jar") && !key.endsWith("-sources.jar")) {
                map2.put(mcversion, url);
            } else if (key.endsWith(".json")) {
                map3.put(mcversion, url);
            }
        }

        File latest = new File("build/libs/latest.json");
        FileUtils.write(latest, gson.toJson(map), StandardCharsets.UTF_8);
        CosUtil.uploadFile("latest.json", latest);
    }

    @TaskAction
    public void upload() throws IOException {
        if (System.getenv("COS_SECRET_KEY") != null) {
            uploadArtifacts();
        }
    }
}
