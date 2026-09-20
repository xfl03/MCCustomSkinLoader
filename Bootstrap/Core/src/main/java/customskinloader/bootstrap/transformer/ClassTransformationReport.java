package customskinloader.bootstrap.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;

public final class ClassTransformationReport {
    private final String internalClassName;
    private final ClassNode transformedClassNode;
    private final byte[] originalBytecode;
    private final List<String> appliedRuleNames;
    private volatile byte[] transformedBytecode;

    ClassTransformationReport(String internalClassName, ClassNode transformedClassNode, byte[] originalBytecode, List<String> appliedRuleNames) {
        this.internalClassName = internalClassName;
        this.transformedClassNode = transformedClassNode;
        this.originalBytecode = originalBytecode;
        this.appliedRuleNames = Collections.unmodifiableList(new ArrayList<>(appliedRuleNames));
    }

    public byte[] getTransformedBytecode() {
        if (!this.isModified() && this.originalBytecode != null) {
            return this.originalBytecode.clone();
        }
        if (this.transformedBytecode == null) {
            if (this.transformedClassNode == null) {
                throw new IllegalStateException("Transformed bytecode is not available without a ClassNode");
            }
            this.transformedBytecode = TransformerBootstrapSupport.toByteArray(this.transformedClassNode);
        }
        return this.transformedBytecode.clone();
    }

    public ClassNode getTransformedClassNode() {
        return this.transformedClassNode;
    }

    public List<String> getAppliedRuleNames() {
        return this.appliedRuleNames;
    }

    public boolean isModified() {
        return !this.appliedRuleNames.isEmpty();
    }
}
