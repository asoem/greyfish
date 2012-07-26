package org.asoem.greyfish.utils.math;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * User: christoph
 * Date: 23.07.12
 * Time: 16:06
 */
public class MathFunctions {

    /**
     * Gaussian function using {@link #exp(double)}
     * @param x x
     * @param norm the normalization factor
     * @param mean the mean
     * @param sigma the standard deviation
     * @return norm * e ^ -((x - mean)^2 / 2 * sigma^2)
     */
    public static double gaussian(double x, double norm, double mean, double sigma) {
        return gaussian2(x - mean, norm, 1 / (2 * sigma * sigma));
    }

    /**
     * Gaussian function with partially precomputed arguments using {@link #exp(double)}
     * @param xMinusMean x - mean
     * @param norm normalization factor
     * @param i2s2 1 / 2 * sigma * sigma
     * @return norm * e ^ -(xMinusMean^2 * i2s2)
     */
    public static double gaussian2(double xMinusMean, double norm, double i2s2) {
        return norm * exp(-xMinusMean * xMinusMean * i2s2);
    }

    /**
     * An exp approximation based on the article
     * "A Fast, Compact Approximation of the Exponential Function", Nicol N. Schraudolph, Neural Computation (1999)
     * @param x the exponent
     * @return an approximated value for e^x
     */
    public static double exp(double x) {
        final long tmp = (long) (1512775 * x + 1072632447);
        return Double.longBitsToDouble(tmp << 32);
    }

    public static UnivariateFunction gaussianFunction(final double norm, final double mean, final double sigma) {
         return new UnivariateFunction() {

             private double i2s2 = 1 / 2 * sigma * sigma;

             @Override
             public double value(double x) {
                 return gaussian2(x - mean, norm, i2s2);
             }
         };
    }

    public static BivariateFunction gaussianFunctionUnknownMean(final double norm, final double sigma) {
        return new BivariateFunction() {

            private double i2s2 = 1 / 2 * sigma * sigma;

            @Override
            public double value(double x, double mean) {
                return gaussian2(x - mean, norm, i2s2);
            }
        };
    }
}
