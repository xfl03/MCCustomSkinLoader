package customskinloader.gradle.util;

import java.util.Collections;
import java.util.Optional;

import customskinloader.gradle.task.SourceSetsSetupTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;

/**
 * If you want to add dependent project sources for annotation processors in current project,
 * use `SourceUtil.addDependencies project, project(":xxx")` instead of `dependencies { implementation project(":xxx") }`
 */
public class SourceUtil {
    public static void addDependencies(Project project, Project... otherProjects) {
        if (project.getTasks().findByName("setupSourceSets") == null) {
            project.getTasks().create("setupSourceSets", SourceSetsSetupTask.class, task -> {
                task.project = project;
                Collections.addAll(task.otherProjects, otherProjects);
                Optional.ofNullable(project.getTasks().findByName("deobfCompileDummyTask")).ifPresent(task0 -> task0.dependsOn(task));
            });
        }

        for (Project otherProject : otherProjects) {
            Dependency dependency = project.getDependencies().add("implementation", otherProject);
            if (dependency instanceof ModuleDependency) {
                ((ModuleDependency) dependency).setTransitive(false);
            }
        }
    }
}
