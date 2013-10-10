package org.asoem.greyfish.utils.math;

/**
 * Constants for commonly used significance levels [1].
 * <h2>References</h2>
 * <ol>
 *     <li><a href="http://en.wikipedia.org/wiki/Statistical_significance">http://en.wikipedia.org/wiki/Statistical_significance</a></li>
 * </ol>
 */
public enum SignificanceLevel {
    LOW_SIGNIFICANT(0.1, ""),
    SIGNIFICANT(0.05, "*"),
    VERY_SIGNIFICANT(0.01, "**"),
    HIGHLY_SIGNIFICANT(0.001, "***");

    private final double alpha;
    private final String symbol;

    SignificanceLevel(final double alpha, final String symbol) {
        this.alpha = alpha;
        this.symbol = symbol;
    }

    public double getAlpha() {
        return alpha;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "p <= " + alpha + " " + getSymbol();
    }

    public boolean check(final double p) {
        return p <= this.alpha;
    }
}
