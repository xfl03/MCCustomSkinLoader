package customskinloader.bootstrap.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;

public final class ClassTransformationReport {
    private final ClassTransformationContext context;
    private final List<String> appliedTransformerNames;
    private volatile byte[] transformedBytecode;

    public ClassTransformationReport(ClassTransformationContext context, List<String> appliedTransformerNames) {
        this.context = context;
        this.appliedTransformerNames = Collections.unmodifiableList(new ArrayList<>(appliedTransformerNames));
    }

    public ClassTransformationContext getContext() {
        return this.context;
    }

    public byte[] getTransformedBytecode() {
        if (this.transformedBytecode == null) {
            if (this.getTransformedClassNode() == null) {
                throw new IllegalStateException("Transformed bytecode is not available without a transformed ClassNode");
            }

            this.transformedBytecode = TransformerBootstrapSupport.toByteArray(this.getTransformedClassNode());
        }

        return this.transformedBytecode.clone();
    }

    public ClassNode getTransformedClassNode() {
        return this.context.getCurrentClassNode();
    }

    public List<String> getAppliedTransformerNames() {
        return this.appliedTransformerNames;
    }

    public boolean isModified() {
        return !this.appliedTransformerNames.isEmpty();
    }
}
