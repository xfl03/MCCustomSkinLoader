package customskinloader.bootstrap.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import customskinloader.bootstrap.mapping.Mappings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public final class TransformationPlan {
    private static final String IGNORE_PATCH_FAILURE_PROPERTY = "customskinloader.ignorePatchFailure";
    private static final Comparator<TransformationRule> RULE_ORDER = (left, right) -> {
        // Higher-priority transformers run first so low-priority patches can react to earlier changes.
        int priorityOrder = Integer.compare(right.getPriority(), left.getPriority());
        if (priorityOrder != 0) {
            return priorityOrder;
        }
        int groupOrder = left.getGroupName().compareTo(right.getGroupName());
        if (groupOrder != 0) {
            return groupOrder;
        }
        int declarationOrder = Integer.compare(left.getDeclarationOrder(), right.getDeclarationOrder());
        return declarationOrder != 0 ? declarationOrder : left.getId().compareTo(right.getId());
    };

    private final RuntimeVersion runtimeVersion;
    private final Mappings mappings;
    private final TransformationObserver observer;
    private final Map<String, List<TransformationRule>> rulesByTargetClass;
    private final Set<String> targetClassNames;
    private final int declaredRuleCount;
    private final int activeRuleCount;

    public TransformationPlan(Iterable<TransformationRuleProvider> providers, RuntimeVersion runtimeVersion, Mappings mappings, TransformationObserver observer) {
        if (providers == null || runtimeVersion == null || mappings == null || observer == null) {
            throw new IllegalArgumentException("Transformation plan inputs must not be null");
        }

        this.runtimeVersion = runtimeVersion;
        this.mappings = mappings;
        this.observer = observer;

        Map<String, List<TransformationRule>> mutableRulesByTarget = new LinkedHashMap<>();
        Set<String> ruleIds = new HashSet<>();
        Set<String> activeVariants = new HashSet<>();
        int declaredRules = 0;
        int activeRules = 0;
        for (TransformationRuleProvider provider : providers) {
            if (provider == null) {
                throw new IllegalStateException("Transformation rule provider must not be null");
            }

            List<TransformationRule> rules = provider.getTransformationRules();
            if (rules == null) {
                throw new IllegalStateException("Transformation rule provider " + provider.getClass().getName() + " returned null");
            }
            for (TransformationRule rule : rules) {
                declaredRules++;
                if (rule == null) {
                    throw new IllegalStateException("Transformation rule provider " + provider.getClass().getName() + " returned a null rule");
                }
                if (!ruleIds.add(rule.getId())) {
                    throw new IllegalStateException("Duplicate transformation rule id: " + rule.getId());
                }
                if (!rule.getVersionSelector().matches(runtimeVersion)) {
                    continue;
                }

                String mappedTargetClassName = mappings.remapClassName(rule.getTargetClassName()).replace('.', '/');
                if (rule.getVariantGroup() != null && !activeVariants.add(mappedTargetClassName + "@" + rule.getVariantGroup())) {
                    throw new IllegalStateException("Multiple variants from transformation rule group " + rule.getVariantGroup()
                        + " are active for " + mappedTargetClassName + " at " + runtimeVersion);
                }
                mutableRulesByTarget.computeIfAbsent(mappedTargetClassName, ignored -> new ArrayList<>()).add(rule);
                activeRules++;
            }
        }

        Map<String, List<TransformationRule>> compiledRulesByTarget = new LinkedHashMap<>();
        for (Map.Entry<String, List<TransformationRule>> entry : mutableRulesByTarget.entrySet()) {
            List<TransformationRule> sortedRules = new ArrayList<>(entry.getValue());
            Collections.sort(sortedRules, RULE_ORDER);
            compiledRulesByTarget.put(entry.getKey(), Collections.unmodifiableList(sortedRules));
        }

        this.rulesByTargetClass = Collections.unmodifiableMap(compiledRulesByTarget);
        this.targetClassNames = Collections.unmodifiableSet(new LinkedHashSet<>(compiledRulesByTarget.keySet()));
        this.declaredRuleCount = declaredRules;
        this.activeRuleCount = activeRules;
    }

    public Set<String> getTargetClassNames() {
        return this.targetClassNames;
    }

    public int getDeclaredRuleCount() {
        return this.declaredRuleCount;
    }

    public int getActiveRuleCount() {
        return this.activeRuleCount;
    }

    public ClassTransformationReport transform(String className, byte[] classBytecode) {
        if (classBytecode == null) {
            throw new IllegalArgumentException("Class bytecode must not be null");
        }

        String internalClassName = className == null ? "" : className.replace('.', '/');
        List<TransformationRule> rules = this.rulesByTargetClass.get(internalClassName);
        if (rules == null) {
            return new ClassTransformationReport(internalClassName, null, classBytecode, Collections.emptyList());
        }

        ClassNode classNode = new ClassNode();
        new ClassReader(classBytecode).accept(classNode, 0);
        return this.transform(internalClassName, classNode, classBytecode, rules);
    }

    public ClassTransformationReport transform(String className, ClassNode classNode) {
        if (classNode == null) {
            throw new IllegalArgumentException("Class node must not be null");
        }

        String internalClassName = className == null || className.trim().isEmpty() ? classNode.name : className.replace('.', '/');
        List<TransformationRule> rules = this.rulesByTargetClass.get(internalClassName);
        if (rules == null) {
            return new ClassTransformationReport(internalClassName, classNode, null, Collections.emptyList());
        }
        return this.transform(internalClassName, classNode, null, rules);
    }

    private ClassTransformationReport transform(String internalClassName, ClassNode classNode, byte[] originalBytecode, List<TransformationRule> rules) {
        ClassTransformationContext context = ClassTransformationContext.create(internalClassName, classNode, this.mappings);
        List<String> appliedRules = new ArrayList<>();
        for (TransformationRule rule : rules) {
            boolean modified;
            try {
                modified = rule.getOperation().apply(context);
            } catch (Exception exception) {
                String message = "Failed to apply transformation rule " + rule.getId() + " to " + internalClassName;
                throw new IllegalStateException(message, exception);
            }

            if (!modified) {
                String message = "Transformation rule '" + rule.getId() + "' selected " + internalClassName + " for "
                    + this.runtimeVersion + " but did not modify any bytecode. If you want to force CustomSkinLoader to ignore this patch failure, add -D"
                    + IGNORE_PATCH_FAILURE_PROPERTY + "=true to the JVM arguments.";
                String failureSetting = System.getProperty(IGNORE_PATCH_FAILURE_PROPERTY);
                if ("false".equalsIgnoreCase(failureSetting) || (rule.isRequired() && !Boolean.getBoolean(IGNORE_PATCH_FAILURE_PROPERTY))) {
                    throw new IllegalStateException(message);
                }
                this.observer.optionalRuleSkipped(message);
                continue;
            }

            appliedRules.add(rule.getId());
            this.observer.ruleApplied(rule.getId(), internalClassName);
        }

        if (!appliedRules.isEmpty()) {
            this.observer.classTransformed(internalClassName, appliedRules);
        }
        return new ClassTransformationReport(internalClassName, classNode, originalBytecode, appliedRules);
    }
}
