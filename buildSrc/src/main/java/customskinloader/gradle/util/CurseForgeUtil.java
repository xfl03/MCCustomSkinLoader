package customskinloader.gradle.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.matthewprenger.cursegradle.CurseArtifact;
import com.matthewprenger.cursegradle.CurseUploadTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePluginConvention;

// https://www.curseforge.com/minecraft/mc-mods/customskinloader
public class CurseForgeUtil {
    public static void provideCurseForge(Project project) {
        if (!project.getPlugins().hasPlugin("base")) return;

        // We don’t use the task provided by the plugin because it adds some dependencies that we don’t need.
        project.getTasks().create("publishCurseForge", CurseUploadTask.class, task -> {
            task.setGroup("publish");
            task.onlyIf(task0 -> System.getenv("CURSEFORGE_API_KEY") != null);

            task.setApiKey(System.getenv("CURSEFORGE_API_KEY"));
            task.setProjectId("286924");
            task.setMainArtifact(new CurseArtifact() {
                {
                    this.setArtifact(project.getTasks().getByName("jar"));
                    List<Object> gameVersionStrings = new ArrayList<>();
                    Optional.ofNullable(ConfigUtil.getConfigString(project, "minecraft_full_versions"))
                        .ifPresent(versions -> Collections.addAll(gameVersionStrings, versions.split(","))); // Minecraft Versions
                    Optional.ofNullable(ConfigUtil.getConfigString(project, "java_full_versions"))
                        .ifPresent(versions -> {
                            for (String javaVersion : versions.split(",")) {
                                gameVersionStrings.add("Java " + javaVersion); // Java Versions
                            }
                        });
                    gameVersionStrings.add(project.getName().split("/")[0]); // Loader Version
                    this.setGameVersionStrings(gameVersionStrings);
                    this.setChangelogType("text");
                    this.setChangelog(System.getenv("GIT_COMMIT_DESC"));
                    this.setDisplayName(project.getConvention().getPlugin(BasePluginConvention.class).getArchivesBaseName() + "-" + VersionUtil.getShortVersion(project.getRootProject()));
                    this.setReleaseType(VersionUtil.isSnapshot(project.getRootProject()) ? "beta" : "release");
                }
            });
            task.setAdditionalArtifacts(new ArrayList<>());

            TaskUtil.withTask(project.getRootProject(), "upload", task0 -> task0.finalizedBy(task));
        });
    }
}
