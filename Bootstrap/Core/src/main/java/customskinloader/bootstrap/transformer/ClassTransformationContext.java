package customskinloader.bootstrap.transformer;

import customskinloader.bootstrap.mapping.Mappings;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public final class ClassTransformationContext {
    private final String internalClassName;
    private final ClassNode currentClassNode;
    private final Mappings mappings;

    private ClassTransformationContext(String internalClassName, ClassNode currentClassNode, Mappings mappings) {
        this.internalClassName = internalClassName;
        this.currentClassNode = currentClassNode;
        this.mappings = mappings == null ? Mappings.EMPTY : mappings;
    }

    public static ClassTransformationContext create(String className, ClassNode classNode, Mappings mappings) {
        if (classNode == null) {
            throw new IllegalArgumentException("Class node must not be null");
        }

        return new ClassTransformationContext(normalizeInternalClassName(className, classNode), classNode, mappings);
    }

    public String getInternalClassName() {
        return this.internalClassName;
    }

    public ClassNode getCurrentClassNode() {
        return this.currentClassNode;
    }

    public Mappings getMappings() {
        return this.mappings;
    }

    public String remapClassName(String sourceName) {
        return this.mappings.remapClassName(sourceName);
    }

    public String unmapClassName(String targetName) {
        return this.mappings.unmapClassName(targetName);
    }

    public String remapFieldName(String sourceOwner, String sourceName) {
        return this.mappings.remapFieldName(sourceOwner, sourceName);
    }

    public String unmapFieldName(String targetOwner, String targetName) {
        return this.mappings.unmapFieldName(targetOwner, targetName);
    }

    public String remapMethodName(String sourceOwner, String sourceName, String sourceDescriptor) {
        return this.mappings.remapMethodName(sourceOwner, sourceName, sourceDescriptor);
    }

    public String unmapMethodName(String targetOwner, String targetName, String targetDescriptor) {
        return this.mappings.unmapMethodName(targetOwner, targetName, targetDescriptor);
    }

    public String remapMethodDescriptor(String sourceDescriptor) {
        return this.mappings.remapMethodDescriptor(sourceDescriptor);
    }

    public String unmapMethodDescriptor(String targetDescriptor) {
        return this.mappings.unmapMethodDescriptor(targetDescriptor);
    }

    public boolean isTarget(String canonicalName) {
        return this.getInternalClassName().equals(this.remapClassName(canonicalName));
    }

    public MethodNode findMethod(String owner, String name, String desc) {
        String mappedDesc = this.remapMethodDescriptor(desc);
        String mappedName = "<init>".equals(name) ? name : this.remapMethodName(owner, name, desc);
        for (MethodNode methodNode : this.getCurrentClassNode().methods) {
            if (mappedName.equals(methodNode.name) && mappedDesc.equals(methodNode.desc)) {
                return methodNode;
            }
        }
        return null;
    }

    public FieldNode findField(String owner, String name) {
        String mappedName = this.remapFieldName(owner, name);
        for (FieldNode fieldNode : this.getCurrentClassNode().fields) {
            if (mappedName.equals(fieldNode.name)) {
                return fieldNode;
            }
        }
        return null;
    }

    private static String normalizeInternalClassName(String className, ClassNode classNode) {
        if (className != null && !className.trim().isEmpty()) {
            return normalizeInternalClassName(className);
        }
        if (classNode == null || classNode.name == null || classNode.name.trim().isEmpty()) {
            throw new IllegalArgumentException("Class name must be available from either the context or the ClassNode");
        }

        return normalizeInternalClassName(classNode.name);
    }

    private static String normalizeInternalClassName(String className) {
        return className.replace('.', '/');
    }
}
