package customskinloader.bootstrap.transformer;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class TargetedClassTransformer {
    private final String name;
    private final int priority;
    private final Set<String> targetClassNames;

    protected TargetedClassTransformer(String name, int priority, String... targetClassNames) {
        this.name = name;
        this.priority = priority;
        this.targetClassNames = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(targetClassNames)));
    }

    public final String getName() {
        return this.name;
    }

    public final int getPriority() {
        return this.priority;
    }

    public boolean supports(String className, customskinloader.bootstrap.mapping.Mappings mappings) {
        if (this.targetClassNames.isEmpty()) {
            return false;
        }

        for (String targetClassName : this.targetClassNames) {
            if (className.equals(mappings.remapClassName(targetClassName))) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getTargetClassNames() {
        return this.targetClassNames;
    }

    public abstract boolean transform(ClassTransformationContext context) throws Exception;
}
