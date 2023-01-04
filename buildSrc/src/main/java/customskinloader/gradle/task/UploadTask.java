package customskinloader.gradle.task;

import java.io.File;
import java.io.IOException;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import customskinloader.gradle.entity.CslDetail;
import customskinloader.gradle.entity.CslLatest;
import customskinloader.gradle.util.CdnUtil;
import customskinloader.gradle.util.ConfigUtil;
import customskinloader.gradle.util.CosUtil;
import customskinloader.gradle.util.VersionUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

public class UploadTask extends DefaultTask {
    public Project rootProject;

    private CslLatest uploadArtifacts(String filename) throws IOException {
        String shortVersion = VersionUtil.getShortVersion(rootProject);
        File dir = rootProject.file("build/libs");
        if (!dir.isDirectory()) {
            return null;
        }

        String cslversion = shortVersion.replace(".", "");
        CslLatest latest = new CslLatest(shortVersion);

        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            String key = CosUtil.getKey(file.getName());
            if (key == null) {
                continue;
            }
            CosUtil.uploadFile(key, file);
            String url = CdnUtil.CDN_ROOT + key;
            String mcversion = VersionUtil.getMcVersion(file.getName());
            if (mcversion.equals("ForgeLegacy")) {
                mcversion = "Forge";
            }
            System.out.printf("csl-%s-%s\t%s%n",
                    mcversion.replace(".", "").toLowerCase(), cslversion, url);

            if (key.startsWith("mods/") && key.endsWith(".jar") && !key.endsWith("-sources.jar")) {
                latest.downloads.put(mcversion, url);
            } else if (key.endsWith(".json")) {
                latest.launchermeta.put(mcversion, url);
            }
        }

        CosUtil.writeAndUploadObject(filename, latest);
        return latest;
    }

    private void uploadDetail(CslLatest latest, String filename) throws IOException {
        CslDetail detail = new CslDetail(latest.version);

        rootProject.getAllprojects().stream()
                .filter(it -> !"false".equals(ConfigUtil.getConfigString(it, "is_real_project")))
                .forEach(project -> {
                    String edition = VersionUtil.getEdition(project);
                    String url = latest.getUrl(edition);
                    VersionUtil.getMcMajorVersions(
                                    ConfigUtil.getConfigString(project, "minecraft_full_versions"))
                            .forEach(mcMajorVersion -> detail.addDetail(mcMajorVersion, edition, url));
                });

        detail.sortDetails();
        CosUtil.writeAndUploadObject(filename, detail);
    }

    protected void uploadBase(String latestJsonName, String detailJsonName) throws IOException, TencentCloudSDKException {
        if (System.getenv("COS_SECRET_KEY") == null) {
            return;
        }
        CslLatest latest = uploadArtifacts(latestJsonName);
        if (latest == null) {
            return;
        }
        uploadDetail(latest, detailJsonName);
        CdnUtil.updateCdn(latestJsonName, detailJsonName);
    }

    @TaskAction
    public void upload() throws IOException, TencentCloudSDKException {
        uploadBase("latest.json","detail.json");
    }
}
