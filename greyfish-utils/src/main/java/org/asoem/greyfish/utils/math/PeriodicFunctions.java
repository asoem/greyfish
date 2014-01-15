package org.asoem.greyfish.utils.math;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.util.FastMath;

import static com.google.common.base.Preconditions.checkArgument;

public final class PeriodicFunctions {

    private PeriodicFunctions() {
        throw new AssertionError("Not instantiable");
    }

    public static UnivariateFunction triangleWave(final double period, final double phase) {
        checkArgument(period > 0, "Period should be > 0, was %s", period);
        checkArgument(phase >= 0 && phase < 1, "Phase expected in [0,1), was %s", phase);

        return new UnivariateFunction() {
            private final UnivariateFunction sawtoothWave =
                    sawtoothWave(period, phase < 0.75 ? phase + 0.25 : -0.75 + phase);

            @Override
            public double value(final double x) {
                return 2 * FastMath.abs(sawtoothWave.value(x)) - 1;
            }
        };
    }

    public static UnivariateFunction sawtoothWave(final double period, final double phase) {
        checkArgument(period > 0, "Period should be > 0, was %s", period);
        checkArgument(phase >= 0 && phase < 1, "Phase expected in [0,1), was %s", phase);

        if (period == 1.0) {
            return new UnivariateFunction() {
                @Override
                public double value(final double x) {
                    final double xPlusPhase = x + phase;
                    return 2 * (xPlusPhase - FastMath.floor(0.5 + xPlusPhase));
                }
            };
        } else {
            return new UnivariateFunction() {
                private final double absolutePhaseShift = phase * period;

                @Override
                public double value(final double x) {
                    final double xPlusPhase = (x + absolutePhaseShift) / period;
                    return 2 * (xPlusPhase - FastMath.floor(0.5 + xPlusPhase));
                }
            };
        }
    }

    public static UnivariateFunction squareWave(final double period, final double phase) {
        checkArgument(period > 0, "Period should be > 0, was %s", period);
        checkArgument(phase >= 0 && phase < 1, "Phase expected in [0,1), was %s", phase);

        return new UnivariateFunction() {
            private final UnivariateFunction sawtoothWave = sawtoothWave(period, phase);

            @Override
            public double value(final double x) {
                return sawtoothWave.value(x) >= 0 ? 1.0 : -1.0;
            }
        };
    }
}
