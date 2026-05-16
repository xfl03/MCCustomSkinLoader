package customskinloader.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public final class ManifestLibrariesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("manifestLibraries", ManifestLibrariesExtension.class, project);
    }
}
