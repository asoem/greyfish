/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
