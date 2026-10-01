package customskinloader.bootstrap.transformer;

import customskinloader.bootstrap.util.RangeMatcher;

public final class VersionSelector {
    private final String expression;

    public VersionSelector(String expression) {
        this.expression = expression == null ? "" : expression.trim();
    }

    public boolean matches(RuntimeVersion version) {
        return RangeMatcher.matches(this.expression, version.getProtocolVersion(), version.getWorldVersion());
    }

    @Override
    public String toString() {
        return this.expression.isEmpty() ? "*" : this.expression;
    }
}
