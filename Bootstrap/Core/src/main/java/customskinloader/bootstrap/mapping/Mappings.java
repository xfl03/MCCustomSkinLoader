package customskinloader.bootstrap.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Mappings {
    public static final Mappings EMPTY = new Mappings(Collections.emptyList());

    private static final Pattern CLASS_NAME_IN_DESCRIPTOR = Pattern.compile("L([^;]+);");

    private final List<ClassMapping> classMappings;
    private final Map<String, ClassMapping> classMappingsBySourceName;
    private final Map<String, ClassMapping> classMappingsByTargetName;
    private final Map<String, FieldMapping> fieldMappingsBySourceKey;
    private final Map<String, FieldMapping> fieldMappingsByTargetKey;
    private final Map<String, MethodMapping> methodMappingsBySourceKey;
    private final Map<String, MethodMapping> methodMappingsByTargetKey;

    public Mappings(List<ClassMapping> classMappings) {
        List<ClassMapping> copiedClassMappings = Collections.unmodifiableList(new ArrayList<>(classMappings));
        Map<String, ClassMapping> sourceIndex = new LinkedHashMap<>();
        Map<String, ClassMapping> targetIndex = new LinkedHashMap<>();
        Map<String, String> sourceToTargetClassNames = new LinkedHashMap<>();
        Map<String, String> targetToSourceClassNames = new LinkedHashMap<>();

        for (ClassMapping classMapping : copiedClassMappings) {
            String normalizedSourceName = normalizeClassName(classMapping.getSourceName());
            String normalizedTargetName = normalizeClassName(classMapping.getTargetName());
            sourceIndex.put(normalizedSourceName, classMapping);
            targetIndex.put(normalizedTargetName, classMapping);
            sourceToTargetClassNames.put(normalizedSourceName, normalizedTargetName);
            targetToSourceClassNames.put(normalizedTargetName, normalizedSourceName);
        }

        Map<String, FieldMapping> sourceFieldIndex = new LinkedHashMap<>();
        Map<String, FieldMapping> targetFieldIndex = new LinkedHashMap<>();
        Map<String, MethodMapping> sourceMethodIndex = new LinkedHashMap<>();
        Map<String, MethodMapping> targetMethodIndex = new LinkedHashMap<>();

        for (ClassMapping classMapping : copiedClassMappings) {
            String normalizedSourceOwner = normalizeClassName(classMapping.getSourceName());
            String normalizedTargetOwner = normalizeClassName(classMapping.getTargetName());

            for (FieldMapping fieldMapping : classMapping.getFieldMappings()) {
                sourceFieldIndex.put(fieldKey(normalizedSourceOwner, fieldMapping.getSourceName()), fieldMapping);
                targetFieldIndex.put(fieldKey(normalizedTargetOwner, fieldMapping.getTargetName()), fieldMapping);
            }

            for (MethodMapping methodMapping : classMapping.getMethodMappings()) {
                sourceMethodIndex.put(methodKey(normalizedSourceOwner, methodMapping.getSourceName(), methodMapping.getSourceDescriptor()), methodMapping);
                targetMethodIndex.put(
                    methodKey(
                        normalizedTargetOwner,
                        methodMapping.getTargetName(),
                        remapDescriptor(methodMapping.getSourceDescriptor(), sourceToTargetClassNames)
                    ),
                    methodMapping
                );
            }
        }

        this.classMappings = copiedClassMappings;
        this.classMappingsBySourceName = Collections.unmodifiableMap(sourceIndex);
        this.classMappingsByTargetName = Collections.unmodifiableMap(targetIndex);
        this.fieldMappingsBySourceKey = Collections.unmodifiableMap(sourceFieldIndex);
        this.fieldMappingsByTargetKey = Collections.unmodifiableMap(targetFieldIndex);
        this.methodMappingsBySourceKey = Collections.unmodifiableMap(sourceMethodIndex);
        this.methodMappingsByTargetKey = Collections.unmodifiableMap(targetMethodIndex);
    }

    public List<ClassMapping> getClassMappings() {
        return this.classMappings;
    }

    public ClassMapping getClassMappingBySourceName(String sourceName) {
        return this.classMappingsBySourceName.get(normalizeClassName(sourceName));
    }

    public ClassMapping getClassMappingByTargetName(String targetName) {
        return this.classMappingsByTargetName.get(normalizeClassName(targetName));
    }

    public String remapClassName(String sourceName) {
        ClassMapping classMapping = this.getClassMappingBySourceName(sourceName);
        return classMapping == null ? normalizeClassName(sourceName) : normalizeClassName(classMapping.getTargetName());
    }

    public String unmapClassName(String targetName) {
        ClassMapping classMapping = this.getClassMappingByTargetName(targetName);
        return classMapping == null ? normalizeClassName(targetName) : normalizeClassName(classMapping.getSourceName());
    }

    public String remapFieldName(String sourceOwner, String sourceName) {
        FieldMapping fieldMapping = this.fieldMappingsBySourceKey.get(fieldKey(normalizeClassName(sourceOwner), sourceName));
        return fieldMapping == null ? sourceName : fieldMapping.getTargetName();
    }

    public String unmapFieldName(String targetOwner, String targetName) {
        FieldMapping fieldMapping = this.fieldMappingsByTargetKey.get(fieldKey(normalizeClassName(targetOwner), targetName));
        return fieldMapping == null ? targetName : fieldMapping.getSourceName();
    }

    public String remapMethodName(String sourceOwner, String sourceName, String sourceDescriptor) {
        MethodMapping methodMapping = this.methodMappingsBySourceKey.get(methodKey(normalizeClassName(sourceOwner), sourceName, sourceDescriptor));
        return methodMapping == null ? sourceName : methodMapping.getTargetName();
    }

    public String unmapMethodName(String targetOwner, String targetName, String targetDescriptor) {
        MethodMapping methodMapping = this.methodMappingsByTargetKey.get(methodKey(normalizeClassName(targetOwner), targetName, targetDescriptor));
        return methodMapping == null ? targetName : methodMapping.getSourceName();
    }

    public String remapMethodDescriptor(String sourceDescriptor) {
        return remapDescriptor(sourceDescriptor, true);
    }

    public String unmapMethodDescriptor(String targetDescriptor) {
        return remapDescriptor(targetDescriptor, false);
    }

    private String remapDescriptor(String descriptor, boolean sourceToTarget) {
        if (descriptor == null || descriptor.isEmpty()) {
            return descriptor;
        }

        Matcher matcher = CLASS_NAME_IN_DESCRIPTOR.matcher(descriptor);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String className = matcher.group(1);
            String mappedName = sourceToTarget ? this.remapClassName(className) : this.unmapClassName(className);
            matcher.appendReplacement(stringBuffer, "L" + Matcher.quoteReplacement(mappedName) + ";");
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private static String remapDescriptor(String descriptor, Map<String, String> classNameMappings) {
        if (descriptor == null || descriptor.isEmpty()) {
            return descriptor;
        }

        Matcher matcher = CLASS_NAME_IN_DESCRIPTOR.matcher(descriptor);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String className = matcher.group(1);
            String mappedName = classNameMappings.get(className);
            if (mappedName == null) {
                mappedName = className;
            }
            matcher.appendReplacement(stringBuffer, "L" + Matcher.quoteReplacement(mappedName) + ";");
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private static String normalizeClassName(String className) {
        return className == null ? null : className.replace('.', '/');
    }

    private static String fieldKey(String owner, String name) {
        return owner + '#' + name;
    }

    private static String methodKey(String owner, String name, String descriptor) {
        return owner + '#' + name + descriptor;
    }

    public static final class ClassMapping {
        private final String sourceName;
        private final String targetName;
        private final List<FieldMapping> fieldMappings = new ArrayList<>();
        private final List<MethodMapping> methodMappings = new ArrayList<>();

        public ClassMapping(String sourceName, String targetName) {
            this.sourceName = sourceName;
            this.targetName = targetName;
        }

        public String getSourceName() {
            return this.sourceName;
        }

        public String getTargetName() {
            return this.targetName;
        }

        public List<FieldMapping> getFieldMappings() {
            return Collections.unmodifiableList(this.fieldMappings);
        }

        public List<MethodMapping> getMethodMappings() {
            return Collections.unmodifiableList(this.methodMappings);
        }

        void addFieldMapping(FieldMapping fieldMapping) {
            this.fieldMappings.add(fieldMapping);
        }

        void addMethodMapping(MethodMapping methodMapping) {
            this.methodMappings.add(methodMapping);
        }
    }

    public static final class FieldMapping {
        private final ClassMapping owner;
        private final String sourceName;
        private final String targetName;

        public FieldMapping(ClassMapping owner, String sourceName, String targetName) {
            this.owner = owner;
            this.sourceName = sourceName;
            this.targetName = targetName;
        }

        public ClassMapping getOwner() {
            return this.owner;
        }

        public String getSourceName() {
            return this.sourceName;
        }

        public String getTargetName() {
            return this.targetName;
        }
    }

    public static final class MethodMapping {
        private final ClassMapping owner;
        private final String sourceName;
        private final String sourceDescriptor;
        private final String targetName;

        public MethodMapping(ClassMapping owner, String sourceName, String sourceDescriptor, String targetName) {
            this.owner = owner;
            this.sourceName = sourceName;
            this.sourceDescriptor = sourceDescriptor;
            this.targetName = targetName;
        }

        public ClassMapping getOwner() {
            return this.owner;
        }

        public String getSourceName() {
            return this.sourceName;
        }

        public String getSourceDescriptor() {
            return this.sourceDescriptor;
        }

        public String getTargetName() {
            return this.targetName;
        }
    }
}
