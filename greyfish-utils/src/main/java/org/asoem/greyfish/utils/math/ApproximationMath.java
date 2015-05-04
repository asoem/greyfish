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

import com.google.common.base.Preconditions;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Math functions with less accuracy but increased performance.
 */
public final class ApproximationMath {

    private ApproximationMath() {
        throw new AssertionError("Not instantiable");
    }

    private static final double ZERO_CLOSEST_POSITIVE = Math.nextUp(0.0);

    /**
     * Gaussian function using {@link #exp}
     *
     * @param x x
     * @param norm the normalization factor
     * @param mean the mean
     * @param sigma the standard deviation
     * @return norm * e ^ -((x - mean)^2 / 2 * sigma^2)
     */
    public static double gaussian(final double x, final double norm, final double mean, final double sigma) {
        Preconditions.checkArgument(sigma > 0, "Sigma must be strictly positive, but was %s", sigma);
        return gaussianHelper(x - mean, norm, i2s2(sigma));
    }

    /**
     * Gaussian function with the precomputed value 1 / (2sigma^2) {@link #i2s2}.
     * It uses the exp approximation function {@link #exp} if the exponent is >= -700
     * and the positive double value closest to 0 otherwise ( {@code Math.nextUp(0.0)} ).
     *
     * @param xMinusMean x - mean
     * @param norm normalization factor
     * @param i2s2 1 / 2 * sigma * sigma
     * @return norm * e ^ -(xMinusMean^2 * i2s2)
     */
    private static double gaussianHelper(final double xMinusMean, final double norm, final double i2s2) {
        final double x = -xMinusMean * xMinusMean * i2s2;
        if (x < -700) {
            return norm * ZERO_CLOSEST_POSITIVE;
        } else {
            return norm * exp(x);
        }
    }

    /**
     * A helper function to compute the last factor for {@link #gaussianHelper(double, double, double)}
     * @param sigma the standard deviation for the gaussian function
     * @return 1 / (2sigma^2)
     */
    private static double i2s2(final double sigma) {
        return 1 / (2 * sigma * sigma);
    }

    /**
     * An exp approximation based on the article
     * "A Fast, Compact Approximation of the Exponential Function", Nicol N. Schraudolph, Neural Computation (1999)
     * It has a max relative error of about 3e-2 for |value| &lt; 700.0 or so, and no accuracy at all outside this range.
     *
     * @param x the exponent
     * @return an approximated value for e^x
     */
    public static double exp(final double x) {
        final long tmp = (long) (EXP_A * x + (EXP_B - EXP_C));
        return Double.longBitsToDouble(tmp << 32);
    }
    private static final double EXP_A = Math.pow(2, 20) / Math.log(2);
    private static final double EXP_B = 1023.0 * Math.pow(2, 20);
    private static final double EXP_C = 45799.0;  /* Read article for choice of c values */

    /**
     * Create a gaussian function.
     * @param norm the normalization factor
     * @param mean the mean of the distribution
     * @param sigma the standard deviation of the distribution
     * @return a new gaussian function object
     */
    public static UnivariateFunction gaussianFunction(final double norm, final double mean, final double sigma) {
        Preconditions.checkArgument(sigma > 0, "Sigma must be strictly positive, but was %s", sigma);
        return new GaussianFunction(sigma, mean, norm);
    }

    private static class GaussianFunction implements UnivariateFunction {
        private final double sigma;
        private final double mean;
        private final double norm;

        private final double i2s2;

        public GaussianFunction(final double sigma, final double mean, final double norm) {
            this.sigma = sigma;
            this.mean = mean;
            this.norm = norm;
            this.i2s2 = ApproximationMath.i2s2(sigma);
        }

        @Override
        public double value(final double x) {
            return ApproximationMath.gaussianHelper(x - mean, norm, i2s2);
        }

        @Override
        public String toString() {
            return "GaussianFunction{" +
                    "norm=" + norm +
                    ", mean=" + mean +
                    ", sigma=" + sigma +
                    '}';
        }
    }
}
