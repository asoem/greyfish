package org.asoem.greyfish.utils.math.statistics;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.StatUtils;

final class DefaultFTest implements FTest {
    private final int numeratorDegreesOfFreedom;
    private final int denominatorDegreesOfFreedom;
    private final double ratio;
    private final double p;

    DefaultFTest(final double[] sample1, final double[] sample2) {
        final double variance1 = StatUtils.variance(sample1);
        final double variance2 = StatUtils.variance(sample2);

        this.numeratorDegreesOfFreedom = sample1.length - 1;
        this.denominatorDegreesOfFreedom = sample2.length - 1;
        final FDistribution fDistribution = new FDistribution(numeratorDegreesOfFreedom, denominatorDegreesOfFreedom);
        this.ratio = variance2 / variance1;
        this.p = 2 * (1 - fDistribution.probability(ratio));
    }

    @Override
    public double p() {
        return p;
    }

    @Override
    public double ratio() {
        return ratio;
    }

    @Override
    public int numeratorDegreesOfFreedom() {
        return numeratorDegreesOfFreedom;
    }

    @Override
    public int denominatorDegreesOfFreedom() {
        return denominatorDegreesOfFreedom;
    }
}
