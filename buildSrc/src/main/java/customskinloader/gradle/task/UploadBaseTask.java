package customskinloader.gradle.task;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import customskinloader.gradle.entity.CslDetail;
import customskinloader.gradle.entity.CslLatest;
import customskinloader.gradle.storage.StorageService;
import customskinloader.gradle.util.CdnUtil;
import customskinloader.gradle.util.ConfigUtil;
import customskinloader.gradle.util.StorageUtil;
import customskinloader.gradle.util.VersionUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

public abstract class UploadBaseTask extends DefaultTask {
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
            if (file.getName().endsWith("-sources.jar")) {
                //Don't upload sources jar to cos
                continue;
            }
            String key = StorageUtil.getKey(file.getName());
            if (key == null) {
                continue;
            }
            StorageService.put(key, file);
            String mcversion = VersionUtil.getMcVersion(file.getName());
            System.out.printf("csl-%s-%s\t%s%n",
                    mcversion.replace(".", "").toLowerCase(),
                    cslversion, CdnUtil.CLOUDFLARE_CDN_ROOT + key);

            if (key.startsWith("mods/") && key.endsWith(".jar") && !key.endsWith("-sources.jar")) {
                latest.downloads.put(mcversion, StorageService.BASE_URL + key);
            }
        }

        StorageService.put(filename, latest);
        return latest;
    }

    private void uploadDetail(CslLatest latest, String filename) throws IOException {
        CslDetail detail = new CslDetail(latest.version);

        rootProject.getAllprojects().stream()
                .filter(it -> !"false".equals(ConfigUtil.getConfigString(it, "is_real_project")))
                .forEach(project -> {
                    String edition = VersionUtil.getEdition(project);
                    String url = latest.getUrl(edition);
                    VersionUtil.parseDependencies(ConfigUtil.getConfigString(project, "dependencies"))
                            .forEach((loader, version) -> VersionUtil.getMcMajorVersions(version)
                                .forEach(mcMajorVersion -> detail.addDetail(mcMajorVersion, loader, url)));
                });

        detail.sortDetails();
        StorageService.put(filename, detail);
    }

    protected void uploadBase(String latestJsonName, String detailJsonName) throws IOException, TencentCloudSDKException {
        System.out.printf("latestJsonName: %s, detailJsonName: %s\n", latestJsonName, detailJsonName);
        if (System.getenv("COS_SECRET_KEY") == null) {
            System.out.println("COS_SECRET_KEY not found.");
            return;
        }
        CslLatest latest = uploadArtifacts(latestJsonName);
        if (latest == null) {
            return;
        }
        uploadDetail(latest, detailJsonName);
        CdnUtil.updateCdn(latestJsonName, detailJsonName);
    }
}
