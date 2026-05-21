package customskinloader.bootstrap.mapping;

import java.io.IOException;
import java.io.InputStream;

public final class Loader {
    private static final String MAPPING_RESOURCE = "/customskinloader/mapping.xml";

    private Loader() {
    }

    public static Mappings load(ClassLoader classLoader, String mappingType) {
        if (mappingType == null || mappingType.trim().isEmpty()) {
            return Mappings.EMPTY;
        }

        InputStream resourceStream = classLoader.getResourceAsStream(MAPPING_RESOURCE.substring(1));
        if (resourceStream == null) {
            resourceStream = Loader.class.getResourceAsStream(MAPPING_RESOURCE);
        }
        if (resourceStream == null) {
            throw new IllegalStateException("Could not find bundled mapping resource " + MAPPING_RESOURCE);
        }

        try (InputStream inputStream = resourceStream) {
            return new Parser().parse(inputStream, mappingType);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse bundled mapping resource " + MAPPING_RESOURCE, exception);
        }
    }
}
