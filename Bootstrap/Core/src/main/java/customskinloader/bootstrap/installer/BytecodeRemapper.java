package customskinloader.bootstrap.installer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import customskinloader.bootstrap.mapping.Mappings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;

final class BytecodeRemapper extends Remapper {
    private final Map<String, String> classMappings = new HashMap<>();
    private final Map<String, String> inverseClassMappings = new HashMap<>();
    private final Map<String, String> fieldMappings = new HashMap<>();
    private final Map<String, String> methodMappings = new HashMap<>();
    private final Map<String, ClassInheritance> classInheritance = new HashMap<>();
    private final Set<String> missingClassInheritance = new HashSet<>();

    BytecodeRemapper(Mappings mappings) {
        for (Mappings.ClassMapping classMapping : mappings.getClassMappings()) {
            this.classMappings.put(classMapping.getSourceName(), classMapping.getTargetName());
            this.inverseClassMappings.put(classMapping.getTargetName(), classMapping.getSourceName());

            for (Mappings.FieldMapping fieldMapping : classMapping.getFieldMappings()) {
                this.fieldMappings.put(this.fieldKey(classMapping.getSourceName(), fieldMapping.getSourceName()), fieldMapping.getTargetName());
            }

            for (Mappings.MethodMapping methodMapping : classMapping.getMethodMappings()) {
                this.methodMappings.put(this.methodKey(classMapping.getSourceName(), methodMapping.getSourceName(), methodMapping.getSourceDescriptor()), methodMapping.getTargetName());
            }
        }
    }

    public void addClassInheritance(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        this.classInheritance.put(classReader.getClassName(), this.readClassInheritance(classReader, false));
    }

    public byte[] remapClass(byte[] classBytes) throws Exception {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(0);
        ClassVisitor classRemapper = (ClassVisitor) findClassRemapper().getConstructor(ClassVisitor.class, Remapper.class).newInstance(classWriter, this);
        classReader.accept(classRemapper, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    private static Class<?> findClassRemapper() throws Exception {
        try {
            return Class.forName("org.objectweb.asm.commons.ClassRemapper");
        } catch (ClassNotFoundException e) {
            return Class.forName("org.objectweb.asm.commons.RemappingClassAdapter");
        }
    }

    public String toClassEntryName(byte[] classBytes) {
        return new ClassReader(classBytes).getClassName() + ".class";
    }

    @Override
    public String map(String internalName) {
        String mappedName = this.classMappings.get(internalName);
        return mappedName == null ? internalName : mappedName;
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        String mappedName = this.fieldMappings.get(this.fieldKey(owner, name));
        if (mappedName != null) {
            return mappedName;
        }

        if (this.hasDeclaredField(owner, name, descriptor)) {
            return name;
        }

        mappedName = this.findInheritedFieldMapping(owner, name, descriptor, new HashSet<>());
        return mappedName == null ? name : mappedName;
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        String mappedName = this.methodMappings.get(this.methodKey(owner, name, descriptor));
        if (mappedName != null || name.charAt(0) == '<') {
            return mappedName == null ? name : mappedName;
        }

        Integer declaredAccess = this.getDeclaredMethodAccess(owner, name, descriptor);
        if (declaredAccess != null && this.blocksInheritedMethodMapping(declaredAccess)) {
            return name;
        }

        mappedName = this.findInheritedMethodMapping(owner, name, descriptor, new HashSet<>());
        return mappedName == null ? name : mappedName;
    }

    private String findInheritedMethodMapping(String owner, String name, String descriptor, Set<String> visitedOwners) {
        if (owner == null || !visitedOwners.add(owner)) {
            return null;
        }

        ClassInheritance inheritance = this.resolveClassInheritance(owner);
        if (inheritance == null) {
            return null;
        }

        String mappedName = this.findMethodMappingInHierarchy(inheritance.superName, name, descriptor, visitedOwners);
        if (mappedName != null) {
            return mappedName;
        }

        for (String interfaceName : inheritance.interfaces) {
            mappedName = this.findMethodMappingInHierarchy(interfaceName, name, descriptor, visitedOwners);
            if (mappedName != null) {
                return mappedName;
            }
        }

        return null;
    }

    private String findInheritedFieldMapping(String owner, String name, String descriptor, Set<String> visitedOwners) {
        if (owner == null || !visitedOwners.add(owner)) {
            return null;
        }

        ClassInheritance inheritance = this.resolveClassInheritance(owner);
        if (inheritance == null) {
            return null;
        }

        for (String interfaceName : inheritance.interfaces) {
            String mappedName = this.findFieldMappingInHierarchy(interfaceName, name, descriptor, visitedOwners);
            if (mappedName != null) {
                return mappedName;
            }
        }

        return this.findFieldMappingInHierarchy(inheritance.superName, name, descriptor, visitedOwners);
    }

    private String findFieldMappingInHierarchy(String owner, String name, String descriptor, Set<String> visitedOwners) {
        if (owner == null || !visitedOwners.add(owner)) {
            return null;
        }

        ClassInheritance inheritance = this.resolveClassInheritance(owner);
        String mappedName = this.fieldMappings.get(this.fieldKey(owner, name));
        if (mappedName != null) {
            return mappedName;
        }
        if (inheritance == null || inheritance.hasDeclaredField(name, descriptor)) {
            return null;
        }

        for (String interfaceName : inheritance.interfaces) {
            mappedName = this.findFieldMappingInHierarchy(interfaceName, name, descriptor, visitedOwners);
            if (mappedName != null) {
                return mappedName;
            }
        }

        return this.findFieldMappingInHierarchy(inheritance.superName, name, descriptor, visitedOwners);
    }

    private String findMethodMappingInHierarchy(String owner, String name, String descriptor, Set<String> visitedOwners) {
        if (owner == null || !visitedOwners.add(owner)) {
            return null;
        }

        ClassInheritance inheritance = this.resolveClassInheritance(owner);
        Integer declaredAccess = inheritance == null ? null : inheritance.getDeclaredMethodAccess(name, descriptor);
        String mappedName = this.methodMappings.get(this.methodKey(owner, name, descriptor));
        if (mappedName != null && (declaredAccess == null || !this.blocksInheritedMethodMapping(declaredAccess))) {
            return mappedName;
        }
        if (inheritance == null) {
            return null;
        }

        mappedName = this.findMethodMappingInHierarchy(inheritance.superName, name, descriptor, visitedOwners);
        if (mappedName != null) {
            return mappedName;
        }

        for (String interfaceName : inheritance.interfaces) {
            mappedName = this.findMethodMappingInHierarchy(interfaceName, name, descriptor, visitedOwners);
            if (mappedName != null) {
                return mappedName;
            }
        }

        return null;
    }

    private ClassInheritance resolveClassInheritance(String sourceName) {
        ClassInheritance inheritance = this.classInheritance.get(sourceName);
        if (inheritance != null || sourceName.startsWith("java/") || this.missingClassInheritance.contains(sourceName)) {
            return inheritance;
        }

        inheritance = this.loadClassInheritance(sourceName);
        if (inheritance == null) {
            this.missingClassInheritance.add(sourceName);
            return null;
        }

        this.classInheritance.put(sourceName, inheritance);
        return inheritance;
    }

    private ClassInheritance loadClassInheritance(String sourceName) {
        String targetName = this.map(sourceName);
        InputStream inputStream = this.getResourceAsStream(targetName + ".class");
        if (inputStream == null) {
            return null;
        }

        try {
            try (InputStream classInputStream = inputStream) {
                return this.readClassInheritance(new ClassReader(classInputStream), true);
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    private ClassInheritance readClassInheritance(final ClassReader classReader, final boolean unmapNames) {
        final Set<String> declaredFields = new HashSet<>();
        final Map<String, Integer> declaredMethods = new HashMap<>();
        classReader.accept(new ClassVisitor(getASMVersion()) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                String normalizedDescriptor = unmapNames ? BytecodeRemapper.this.unmapDescriptor(descriptor) : descriptor;
                declaredFields.add(BytecodeRemapper.this.fieldSignatureKey(name, normalizedDescriptor));
                return null;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                String normalizedDescriptor = unmapNames ? BytecodeRemapper.this.unmapDescriptor(descriptor) : descriptor;
                declaredMethods.put(BytecodeRemapper.this.methodSignatureKey(name, normalizedDescriptor), access);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        return new ClassInheritance(
            unmapNames ? this.unmapClassName(classReader.getSuperName()) : classReader.getSuperName(),
            unmapNames ? this.unmapClassNames(classReader.getInterfaces()) : classReader.getInterfaces(),
            declaredFields,
            declaredMethods
        );
    }

    private static int asmVersion = 0;
    private static int getASMVersion() {
        if (asmVersion != 0) {
            return asmVersion;
        }

        asmVersion = Opcodes.ASM4;
        int versionIdx = 5;
        while (true) {
            try {
                Field asmField = Opcodes.class.getField("ASM" + versionIdx);
                asmVersion = (int) asmField.get(null);
                versionIdx++;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                break;
            }
        }
        return asmVersion;
    }

    private InputStream getResourceAsStream(String resourceName) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            InputStream inputStream = contextClassLoader.getResourceAsStream(resourceName);
            if (inputStream != null) {
                return inputStream;
            }
        }

        ClassLoader ownClassLoader = NestedJarRemapper.class.getClassLoader();
        if (ownClassLoader == null) {
            return ClassLoader.getSystemResourceAsStream(resourceName);
        }

        return ownClassLoader.getResourceAsStream(resourceName);
    }

    private String[] unmapClassNames(String[] classNames) {
        if (classNames == null || classNames.length == 0) {
            return new String[0];
        }

        String[] unmappedClassNames = new String[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            unmappedClassNames[i] = this.unmapClassName(classNames[i]);
        }
        return unmappedClassNames;
    }

    private String unmapClassName(String className) {
        String unmappedName = this.inverseClassMappings.get(className);
        return unmappedName == null ? className : unmappedName;
    }

    private String unmapDescriptor(String descriptor) {
        StringBuilder remappedDescriptor = null;
        int copyStart = 0;

        for (int i = 0; i < descriptor.length(); i++) {
            if (descriptor.charAt(i) != 'L') {
                continue;
            }

            int classNameEnd = descriptor.indexOf(';', i);
            if (classNameEnd < 0) {
                return descriptor;
            }

            String className = descriptor.substring(i + 1, classNameEnd);
            String unmappedName = this.unmapClassName(className);
            if (!className.equals(unmappedName)) {
                if (remappedDescriptor == null) {
                    remappedDescriptor = new StringBuilder(descriptor.length());
                }
                remappedDescriptor.append(descriptor, copyStart, i + 1);
                remappedDescriptor.append(unmappedName);
                copyStart = classNameEnd;
            }

            i = classNameEnd;
        }

        if (remappedDescriptor == null) {
            return descriptor;
        }

        remappedDescriptor.append(descriptor, copyStart, descriptor.length());
        return remappedDescriptor.toString();
    }

    private Integer getDeclaredMethodAccess(String owner, String name, String descriptor) {
        ClassInheritance inheritance = this.resolveClassInheritance(owner);
        return inheritance == null ? null : inheritance.getDeclaredMethodAccess(name, descriptor);
    }

    private boolean hasDeclaredField(String owner, String name, String descriptor) {
        ClassInheritance inheritance = this.resolveClassInheritance(owner);
        return inheritance != null && inheritance.hasDeclaredField(name, descriptor);
    }

    private boolean blocksInheritedMethodMapping(int access) {
        return (access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) != 0;
    }

    private String fieldKey(String owner, String name) {
        return owner + '#' + name;
    }

    private String methodKey(String owner, String name, String descriptor) {
        return owner + '#' + name + descriptor;
    }

    private String fieldSignatureKey(String name, String descriptor) {
        return name + descriptor;
    }

    private String methodSignatureKey(String name, String descriptor) {
        return name + descriptor;
    }

    private static final class ClassInheritance {
        private final String superName;
        private final List<String> interfaces;
        private final Set<String> declaredFields;
        private final Map<String, Integer> declaredMethods;

        ClassInheritance(String superName, String[] interfaces, Set<String> declaredFields, Map<String, Integer> declaredMethods) {
            this.superName = superName;
            if (interfaces == null || interfaces.length == 0) {
                this.interfaces = Collections.emptyList();
            } else {
                this.interfaces = Collections.unmodifiableList(Arrays.asList(interfaces.clone()));
            }
            this.declaredFields = Collections.unmodifiableSet(new HashSet<>(declaredFields));
            this.declaredMethods = Collections.unmodifiableMap(new HashMap<>(declaredMethods));
        }

        boolean hasDeclaredField(String name, String descriptor) {
            return this.declaredFields.contains(name + descriptor);
        }

        Integer getDeclaredMethodAccess(String name, String descriptor) {
            return this.declaredMethods.get(name + descriptor);
        }
    }
}
