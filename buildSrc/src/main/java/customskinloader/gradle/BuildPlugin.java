package customskinloader.gradle;

import customskinloader.gradle.task.UploadTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.printf("Apply project '%s'", project.getName());
        project.getTasks().create("upload", UploadTask.class,
                task -> task.rootProject = project.getRootProject());
    }
}