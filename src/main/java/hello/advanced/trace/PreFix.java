package hello.advanced.trace;

public enum PreFix {
    START_PREFIX("--> "),
    COMPLETE_PREFIX("<-- "),
    EX_PREFIX("X-- ");

    private final String prefix;

    PreFix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return getPrefix();
    }
}
