package customskinloader.bootstrap.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import customskinloader.bootstrap.mapping.Mappings;

public final class ClassTransformerRegistry {
    private static final Comparator<TargetedClassTransformer> TRANSFORMER_ORDER = (left, right) -> {
        // Higher-priority transformers run first so low-priority patches can react to earlier changes.
        int priorityOrder = Integer.compare(right.getPriority(), left.getPriority());
        if (priorityOrder != 0) {
            return priorityOrder;
        }

        return left.getName().compareTo(right.getName());
    };

    private final CopyOnWriteArrayList<TargetedClassTransformer> transformers = new CopyOnWriteArrayList<>();

    public void register(TargetedClassTransformer transformer) {
        this.transformers.add(transformer);
        this.sortTransformers();
    }

    public List<TargetedClassTransformer> getTransformers() {
        return Collections.unmodifiableList(new ArrayList<>(this.transformers));
    }

    public List<TargetedClassTransformer> getApplicableTransformers(String className, Mappings mappings) {
        List<TargetedClassTransformer> applicableTransformers = new ArrayList<>();

        for (TargetedClassTransformer transformer : this.transformers) {
            if (transformer.supports(className, mappings)) {
                applicableTransformers.add(transformer);
            }
        }

        return Collections.unmodifiableList(applicableTransformers);
    }

    private void sortTransformers() {
        List<TargetedClassTransformer> sortedTransformers = new ArrayList<>(this.transformers);
        Collections.sort(sortedTransformers, TRANSFORMER_ORDER);
        this.transformers.clear();
        this.transformers.addAll(sortedTransformers);
    }
}
