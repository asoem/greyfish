package org.asoem.greyfish.utils.math;

import org.apache.commons.math3.analysis.UnivariateFunction;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 21.01.13
 * Time: 14:10
 */
public class PeriodicFunctions {
    private PeriodicFunctions() {}

    public static UnivariateFunction triangleWave(final double frequency, double phase, final double amplitude) {
        checkArgument(frequency > 0);
        checkArgument(amplitude > 0);

        return new UnivariateFunction() {
            private final double quarterFrequency = frequency / 4;
            private final double threeQuarterFrequency = quarterFrequency * 3;
            private final double slope = amplitude / quarterFrequency;

            @Override
            public double value(double x) {
                double xMod = x % frequency;
                if (xMod < 0) xMod += frequency; // arithmetic modulo

                if (xMod < quarterFrequency) return xMod * slope;
                else if (xMod > threeQuarterFrequency) return -amplitude+(xMod-threeQuarterFrequency)*slope;
                else return amplitude - (xMod - quarterFrequency) * slope;
            }
        };
    }
}
