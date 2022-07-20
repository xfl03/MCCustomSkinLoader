package customskinloader.gradle.task;

import java.util.HashSet;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

/**
 * We need to add sourceSets at compile time to avoid IntelliJ IDEA from removing duplicated source roots and to avoid Eclipse add source dirs twice.
 * The reason of that we don't just dependent other subprojects is to let Mixin AP work correctly.
 */
public class SourceSetsSetupTask extends DefaultTask {
    public Project project;

    public Set<Project> otherProjects = new HashSet<>();

    @TaskAction
    public void setup() {
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("main", sourceSet ->
            this.otherProjects.stream()
                .filter(other -> other.getPlugins().hasPlugin("java"))
                .forEach(other -> {
                    SourceSet otherMain = other.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("main");
                    otherMain.getJava().getSrcDirs().forEach(targetDir ->
                        sourceSet.getJava().srcDir(targetDir)
                    );
                    otherMain.getResources().getSrcDirs().forEach(targetDir ->
                        sourceSet.getResources().srcDir(targetDir)
                    );
                }));
    }
}
