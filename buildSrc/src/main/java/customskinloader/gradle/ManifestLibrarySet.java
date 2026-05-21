package customskinloader.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalModuleDependency;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ManifestLibrarySet {
    private final Set<String> repositoryUrls;
    private final Set<String> dependencyNotations;

    ManifestLibrarySet(Set<String> repositoryUrls, Set<String> dependencyNotations) {
        this.repositoryUrls = Collections.unmodifiableSet(new LinkedHashSet<>(repositoryUrls));
        this.dependencyNotations = Collections.unmodifiableSet(new LinkedHashSet<>(dependencyNotations));
    }

    public void addTo(Project project) {
        addTo(project, "compileOnly");
    }

    public void addTo(Project project, String configuration) {
        for (String repositoryUrl : repositoryUrls) {
            addRepository(project, repositoryUrl);
        }

        for (String notation : dependencyNotations) {
            ExternalModuleDependency dependency = (ExternalModuleDependency) project.getDependencies().create(notation);
            dependency.setTransitive(false);
            project.getDependencies().add(configuration, dependency);
        }
    }

    private static void addRepository(Project project, String url) {
        project.getRepositories().maven(repository -> {
            repository.setUrl(project.uri(url));
            repository.metadataSources(sources -> {
                sources.mavenPom();
                sources.artifact();
            });
        });
    }
}
