package java.lang.module;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Configuration {
    public static Configuration resolve(ModuleFinder before, List<Configuration> parents, ModuleFinder after, Collection<String> roots) {
        return null;
    }

    public static Configuration empty() {
        return null;
    }

    public Optional<ResolvedModule> findModule(String name) {
        return null;
    }
}
