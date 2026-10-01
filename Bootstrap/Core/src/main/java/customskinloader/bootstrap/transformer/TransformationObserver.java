package customskinloader.bootstrap.transformer;

public interface TransformationObserver {
    void ruleApplied(String ruleId, String internalClassName);

    void optionalRuleSkipped(String message);

    void classTransformed(String internalClassName, Iterable<String> ruleIds);
}
