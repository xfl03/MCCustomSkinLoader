package customskinloader.gradle;

import groovy.json.JsonSlurper;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ManifestLibrariesExtension {
    private static final int TIMEOUT_MILLIS = 15_000;

    private final Project project;

    @Inject
    public ManifestLibrariesExtension(Project project) {
        this.project = project;
    }

    public ManifestLibrarySet resolve(String manifestUrl) {
        Set<String> repositoryUrls = new LinkedHashSet<>();
        Set<String> dependencyNotations = new LinkedHashSet<>();

        Map<String, Object> manifest = asObject(readJson(manifestUrl), "manifest root");
        Object librariesValue = manifest.get("libraries");
        if (librariesValue == null) {
            return new ManifestLibrarySet(repositoryUrls, dependencyNotations);
        }

        List<Object> libraries = asList(librariesValue, "libraries");
        for (Object libraryValue : libraries) {
            Map<String, Object> library = asObject(libraryValue, "library");
            if (!isAllowed(library)) {
                continue;
            }

            if (library.get("downloads") != null) {
                addDownloadsLibrary(library, repositoryUrls, dependencyNotations);
            } else {
                addUrlLibrary(library, repositoryUrls, dependencyNotations);
            }
        }

        return new ManifestLibrarySet(repositoryUrls, dependencyNotations);
    }

    public void add(String manifestUrl) {
        add(resolve(manifestUrl));
    }

    public void add(ManifestLibrarySet libraries) {
        libraries.addTo(project);
    }

    public void add(ManifestLibrarySet libraries, Project target) {
        libraries.addTo(target);
    }

    public void add(ManifestLibrarySet libraries, Iterable<?> targets) {
        for (Object target : targets) {
            if (!(target instanceof Project)) {
                throw new GradleException("Expected manifest library target to be a Project, got " + target);
            }
            libraries.addTo((Project) target);
        }
    }

    private Object readJson(String url) {
        try {
            URLConnection connection = new URI(url).toURL().openConnection();
            connection.setRequestProperty("User-Agent", project.getRootProject().getName() + "/" + project.getRootProject().getVersion());
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);

            try (InputStream input = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                return new JsonSlurper().parse(reader);
            }
        } catch (Exception exception) {
            throw new GradleException("Failed to read manifest libraries from " + url, exception);
        }
    }

    private static void addUrlLibrary(Map<String, Object> library, Set<String> repositoryUrls, Set<String> dependencyNotations) {
        String name = asString(library.get("name"));
        String url = asString(library.get("url"));

        if (name != null && !name.isEmpty()) {
            dependencyNotations.add(name);
        }
        if (url != null && !url.isEmpty()) {
            repositoryUrls.add(trimTrailingSlashes(url));
        }
    }

    private static void addDownloadsLibrary(Map<String, Object> library, Set<String> repositoryUrls, Set<String> dependencyNotations) {
        String name = asString(library.get("name"));
        String artifactUrl = artifactUrl(library);
        if (name == null || name.isEmpty() || artifactUrl == null || artifactUrl.isEmpty()) {
            return;
        }

        DerivedArtifact artifact = deriveArtifact(name, artifactUrl);
        repositoryUrls.add(artifact.repositoryUrl);
        dependencyNotations.add(artifact.dependencyNotation);
    }

    @SuppressWarnings("unchecked")
    private static String artifactUrl(Map<String, Object> library) {
        Object downloads = library.get("downloads");
        if (!(downloads instanceof Map)) {
            return null;
        }

        Object artifact = ((Map<String, Object>) downloads).get("artifact");
        if (!(artifact instanceof Map)) {
            return null;
        }

        return asString(((Map<String, Object>) artifact).get("url"));
    }

    private static DerivedArtifact deriveArtifact(String name, String artifactUrl) {
        String[] nameParts = name.split(":");
        if (nameParts.length < 3) {
            throw new GradleException("Expected dependency name to be group:artifact:version, got " + name);
        }

        String group = nameParts[0];
        String[] organizationPath = group.split("\\.");
        URI uri = URI.create(artifactUrl);
        String[] path = trimLeadingSlashes(uri.getPath()).split("/");
        int organizationStart = indexOf(path, organizationPath);
        if (organizationStart < 0) {
            throw new GradleException("Could not find organization path " + group.replace('.', '/') + " in " + artifactUrl);
        }

        int artifactIndex = organizationStart + organizationPath.length;
        int versionIndex = artifactIndex + 1;
        int fileIndex = versionIndex + 1;
        if (path.length != fileIndex + 1) {
            throw new GradleException("Expected Maven artifact path in " + artifactUrl);
        }

        String artifact = path[artifactIndex];
        String version = path[versionIndex];
        String fileName = path[fileIndex];
        String baseName = stripExtension(fileName);
        String prefix = artifact + "-" + version;
        if (!baseName.equals(prefix) && !baseName.startsWith(prefix + "-")) {
            throw new GradleException("Expected " + fileName + " to start with " + prefix);
        }

        String repositoryUrl = uri.getScheme() + "://" + uri.getAuthority() + pathPrefix(path, organizationStart);
        String dependencyNotation = group + ":" + artifact + ":" + version;
        if (!baseName.equals(prefix)) {
            dependencyNotation += ":" + baseName.substring(prefix.length() + 1);
        }

        return new DerivedArtifact(trimTrailingSlashes(repositoryUrl), dependencyNotation);
    }

    private static String stripExtension(String fileName) {
        int extensionStart = fileName.lastIndexOf('.');
        return extensionStart < 0 ? fileName : fileName.substring(0, extensionStart);
    }

    private static int indexOf(String[] values, String[] sequence) {
        for (int i = 0; i <= values.length - sequence.length; i++) {
            boolean matches = true;
            for (int j = 0; j < sequence.length; j++) {
                if (!values[i + j].equals(sequence[j])) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return i;
            }
        }
        return -1;
    }

    private static String pathPrefix(String[] path, int endExclusive) {
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < endExclusive; i++) {
            prefix.append('/').append(path[i]);
        }
        return prefix.toString();
    }

    private static boolean isAllowed(Map<String, Object> library) {
        Object rulesValue = library.get("rules");
        if (rulesValue == null) {
            return true;
        }

        boolean allowed = false;
        for (Object ruleValue : asList(rulesValue, "rules")) {
            Map<String, Object> rule = asObject(ruleValue, "library rule");
            if (matchesOs(rule.get("os"))) {
                allowed = "allow".equals(asString(rule.get("action")));
            }
        }
        return allowed;
    }

    private static boolean matchesOs(Object osValue) {
        if (osValue == null) {
            return true;
        }

        Map<String, Object> os = asObject(osValue, "rule os");
        String name = asString(os.get("name"));
        String arch = asString(os.get("arch"));
        String version = asString(os.get("version"));

        return (name == null || name.equals(currentOs()))
            && (arch == null || arch.equals(System.getProperty("os.arch")))
            && (version == null || System.getProperty("os.version").matches(version));
    }

    private static String currentOs() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            return "windows";
        }
        if (osName.contains("mac") || osName.contains("darwin")) {
            return "osx";
        }
        if (osName.contains("linux") || osName.contains("nix")) {
            return "linux";
        }
        return osName;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object value, String name) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        throw new GradleException("Expected " + name + " to be a JSON object");
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object value, String name) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        throw new GradleException("Expected " + name + " to be a JSON array");
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String trimLeadingSlashes(String value) {
        if (value == null) {
            return "";
        }

        int index = 0;
        while (index < value.length() && value.charAt(index) == '/') {
            index++;
        }
        return value.substring(index);
    }

    private static String trimTrailingSlashes(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    private static final class DerivedArtifact {
        final String repositoryUrl;
        final String dependencyNotation;

        DerivedArtifact(String repositoryUrl, String dependencyNotation) {
            this.repositoryUrl = repositoryUrl;
            this.dependencyNotation = dependencyNotation;
        }
    }
}
