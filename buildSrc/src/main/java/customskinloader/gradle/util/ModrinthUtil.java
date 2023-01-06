package customskinloader.gradle.util;

import java.util.Optional;

import com.modrinth.minotaur.TaskModrinthUpload;
import com.modrinth.minotaur.request.VersionType;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePluginConvention;

// https://modrinth.com/mod/customskinloader
public class ModrinthUtil {
    public static void provideModrinth(Project project) {
        if (!project.getPlugins().hasPlugin("base")) return;

        project.getTasks().create("publishModrinth", TaskModrinthUpload.class, task -> {
            task.setGroup("publish");
            task.onlyIf(task0 -> System.getenv("MODRINTH_TOKEN") != null);

            task.token = System.getenv("MODRINTH_TOKEN");
            task.projectId = "idMHQ4n2";
            task.versionNumber = VersionUtil.getCSLVersion(project.getRootProject()) + "-" + project.getName().replace("/", "");
            task.versionName = project.getConvention().getPlugin(BasePluginConvention.class).getArchivesBaseName() + "-" + VersionUtil.getShortVersion(project.getRootProject());
            task.changelog = System.getenv("GIT_COMMIT_DESC");
            task.uploadFile = project.getTasks().getByName("jar");
            task.versionType = VersionUtil.isSnapshot(project.getRootProject()) ? VersionType.BETA : VersionType.RELEASE;
            task.failSilently = true;
            task.detectLoaders = false;

            Optional.ofNullable(ConfigUtil.getConfigString(project, "minecraft_full_versions"))
                .ifPresent(versions -> {
                    for (String gameVersion : versions.split(",")) {
                        task.addGameVersion(gameVersion); // Minecraft Versions
                    }
                });
            VersionUtil.getLoaders(project).forEach(task::addLoader);

            TaskUtil.withTask(project.getRootProject(), "upload", task0 -> task0.finalizedBy(task));
        });
    }
}
