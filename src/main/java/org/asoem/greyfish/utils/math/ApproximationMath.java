package org.asoem.greyfish.utils.math;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * User: christoph
 * Date: 23.07.12
 * Time: 16:06
 */
public final class ApproximationMath {

    private ApproximationMath() {}

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
    public static double gaussian(double x, double norm, double mean, double sigma) {
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
    private static double gaussianHelper(double xMinusMean, double norm, double i2s2) {
        final double x = -xMinusMean * xMinusMean * i2s2;
        if (x < -700) {
            return norm * ZERO_CLOSEST_POSITIVE;
        }
        else
            return norm * exp(x);
    }

    /**
     * A helper function to compute the last factor for {@link #gaussianHelper(double, double, double)}
     * @param sigma the standard deviation for the gaussian function
     * @return 1 / (2sigma^2)
     */
    private static double i2s2(double sigma) {
        return 1 / (2 * sigma * sigma);
    }

    /**
     * An exp approximation based on the article
     * "A Fast, Compact Approximation of the Exponential Function", Nicol N. Schraudolph, Neural Computation (1999)
     * It has a max relative error of about 3e-2 for |value| < 700.0 or so, and no accuracy at all outside this range.
     *
     * @param x the exponent
     * @return an approximated value for e^x
     */
    public static double exp(double x) {
        final long tmp = (long) (EXP_A * x + (EXP_B - EXP_C));
        return Double.longBitsToDouble(tmp << 32);
    }
    private static final double EXP_A = Math.pow(2, 20) / Math.log(2);
    private static final double EXP_B = 1023.0 * Math.pow(2, 20);
    private static final double EXP_C = 45799.0;  /* Read article for choice of c values */

    public static UnivariateFunction gaussianFunction(final double norm, final double mean, double sigma) {
        Preconditions.checkArgument(sigma > 0);
        final double i2s2 = i2s2(sigma);

        return new UnivariateFunction() {
            @Override
             public double value(double x) {
                 return gaussianHelper(x - mean, norm, i2s2);
             }
         };
    }

    public static void main(String[] args) {
        double x1 = -10.0;
        double x2 = 10.0;
        double stepLength = 0.01;
        final double v = (x2 - x1) / stepLength;
        final UnivariateFunction function = gaussianFunction(1.0, 0.0, 2.0);
        for (int i = 0; i <= v; i++) {
            System.out.println(function.value(x1 + i * stepLength));
        }
    }
}
