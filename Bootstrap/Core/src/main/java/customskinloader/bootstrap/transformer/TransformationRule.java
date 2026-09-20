package customskinloader.bootstrap.transformer;

public final class TransformationRule {
    @FunctionalInterface
    public interface Operation {
        boolean apply(ClassTransformationContext context) throws Exception;
    }

    private final String groupName;
    private final String name;
    private final int priority;
    private final int declarationOrder;
    private final String variantGroup;
    private final String targetClassName;
    private final VersionSelector versionSelector;
    private final boolean required;
    private final Operation operation;

    public TransformationRule(String groupName, String name, int priority, int declarationOrder, String targetClassName, String versionExpression, boolean required, Operation operation) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Transformation rule group name must not be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Transformation rule name must not be empty");
        }
        if (targetClassName == null || targetClassName.trim().isEmpty()) {
            throw new IllegalArgumentException("Transformation rule target class must not be empty");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Transformation rule operation must not be null");
        }

        this.groupName = groupName;
        this.name = name;
        this.priority = priority;
        this.declarationOrder = declarationOrder;
        int variantMarker = name.lastIndexOf(".v");
        boolean numberedVariant = variantMarker > 0 && variantMarker + 2 < name.length();
        for (int index = variantMarker + 2; numberedVariant && index < name.length(); index++) {
            numberedVariant = Character.isDigit(name.charAt(index));
        }
        this.variantGroup = numberedVariant ? groupName + "/" + name.substring(0, variantMarker) : null;
        this.targetClassName = targetClassName.replace('.', '/');
        this.versionSelector = new VersionSelector(versionExpression);
        this.required = required;
        this.operation = operation;
    }

    public String getId() {
        return this.groupName + "/" + this.name;
    }

    public int getPriority() {
        return this.priority;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public int getDeclarationOrder() {
        return this.declarationOrder;
    }

    public String getVariantGroup() {
        return this.variantGroup;
    }

    public String getTargetClassName() {
        return this.targetClassName;
    }

    public VersionSelector getVersionSelector() {
        return this.versionSelector;
    }

    public boolean isRequired() {
        return this.required;
    }

    public Operation getOperation() {
        return this.operation;
    }
}
