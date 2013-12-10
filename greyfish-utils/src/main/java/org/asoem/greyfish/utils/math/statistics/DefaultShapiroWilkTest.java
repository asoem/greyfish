package org.asoem.greyfish.utils.math.statistics;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;

import static org.apache.commons.math3.util.FastMath.*;

final class DefaultShapiroWilkTest implements ShapiroWilkTest {

    private static final double SMALL = 1e-19;

    private static final PolynomialFunction POLYNOMIAL_FUNCTION_GAMMA =
            new PolynomialFunction(new double[]{-2.273, .459});
    private static final PolynomialFunction POLYNOMIAL_FUNCTION_1 =
            new PolynomialFunction(new double[]{0., .221157, -.147981, -2.07119, 4.434685, -2.706056});
    private static final PolynomialFunction POLYNOMIAL_FUNCTION_2 =
            new PolynomialFunction(new double[]{0., .042981, -.293762, -1.752461, 5.682633, -3.582633});
    private static final PolynomialFunction POLYNOMIAL_FUNCTION_3 =
            new PolynomialFunction(new double[]{.544, -.39978, .025054, -6.714e-4});
    private static final PolynomialFunction POLYNOMIAL_FUNCTION_4 =
            new PolynomialFunction(new double[]{1.3822, -.77857, .062767, -.0020322});
    private static final PolynomialFunction POLYNOMIAL_FUNCTION_5 =
            new PolynomialFunction(new double[]{-1.5861, -.31082, -.083751, .0038915});
    private static final PolynomialFunction POLYNOMIAL_FUNCTION_6 =
            new PolynomialFunction(new double[]{-.4803, -.082676, .0030302});
    private static final int MAX_SAMPLE_SIZE = 5000;
    private static final int MIN_SAMPLE_SIZE = 3;
    private static final double SQRT_1DIV2 = 0.70710678;
    private static final NormalDistribution NORMAL_DISTRIBUTION_0_1 = new NormalDistribution(0.0, 1.0);
    private static final int EXACT_P_SAMPLE_SIZE = 3;
    private static final int LOWER_APPROXIMATION_MAX_SAMPLE_SIZE = 11;
    private static final double CLOSE_TO_0 = 1e-99;

    private final double pw;
    private double w;
    private static final double PI_6 = 1.90985931710274;
    private static final double STQR = 1.04719755119660;

    DefaultShapiroWilkTest(final double[] samples) {
        Arrays.sort(samples);
        int nn2 = (int) Math.floor(samples.length / 2.0);
        double[] a = new double[nn2 + 1]; /* 1-based */

        /* Local variables */
        int i, j, i1;

        double ssassx, summ2, ssumm2, range;
        double a1, a2, sa, xi, sx, xx, w1;
        double fac, asa, an25, ssa, sax, rsn, ssx, xsx;

        if (samples.length < MIN_SAMPLE_SIZE) {
            throw new IllegalArgumentException();
        }

        final double an = (double) samples.length;

        if (samples.length == 3) {
            a[1] = SQRT_1DIV2; /* = sqrt(1/2) */
        } else {
            an25 = an + 0.25;
            summ2 = 0.0;
            for (i = 1; i <= nn2; i++) {
                a[i] = NORMAL_DISTRIBUTION_0_1.inverseCumulativeProbability((i - 0.375) / an25);
                final double r = a[i];
                summ2 += r * r;
            }
            summ2 *= 2.0;
            ssumm2 = sqrt(summ2);
            rsn = 1.0 / sqrt(an);
            a1 = POLYNOMIAL_FUNCTION_1.value(rsn) - a[1] / ssumm2;

            /* Normalize a[] */
            if (samples.length > 5) {
                i1 = 3;
                a2 = -a[2] / ssumm2 + POLYNOMIAL_FUNCTION_2.value(rsn);
                fac = sqrt((summ2 - 2. * (a[1] * a[1]) - 2.0 * (a[2] * a[2]))
                        / (1.0 - 2.0 * (a1 * a1) - 2. * (a2 * a2)));
                a[2] = a2;
            } else {
                i1 = 2;
                fac = sqrt((summ2 - 2. * (a[1] * a[1])) /
                        (1.0 - 2.0 * (a1 * a1)));
            }
            a[1] = a1;
            for (i = i1; i <= nn2; i++) {
                a[i] /= -fac;
            }
        }

        /* Check for zero range */
        range = samples[samples.length - 1] - samples[0];
        if (range < SMALL) {
            throw new IllegalArgumentException("Range to small");
        }

        /* Check for correct sort order on range - scaled X */

        /* *ifault = 7; <-- a no-op, since it is changed below, in ANY CASE! */
        xx = samples[0] / range;
        sx = xx;
        sa = -a[1];
        for (i = 1, j = samples.length - 1; i < samples.length; j--) {
            xi = samples[i] / range;
            if (xx - xi > SMALL) {
                /* Fortran had: print *, "ANYTHING"
                * but do NOT; it *does* happen with sorted x (on Intel GNU/linux 32bit):
                * shapiro.test(c(-1.7, -1,-1,-.73,-.61,-.5,-.24,.45,.62,.81,1))
                */
                throw new IllegalArgumentException();
            }
            sx += xi;
            i++;
            if (i != j) {
                sa += FastMath.signum(i - j) * a[min(i, j)];
            }
            xx = xi;
        }
        if (samples.length > MAX_SAMPLE_SIZE) {
            throw new IllegalArgumentException();
        }

        /* Calculate W statistic as squared correlation
        between data and coefficients */

        sa /= samples.length;
        sx /= samples.length;
        ssa = 0.0;
        ssx = 0.0;
        sax = 0.0;
        for (i = 0, j = samples.length - 1; i < samples.length; i++, j--) {
            if (i != j) {
                asa = FastMath.signum(i - j) * a[1 + min(i, j)] - sa;
            } else {
                asa = -sa;
            }
            xsx = samples[i] / range - sx;
            ssa += asa * asa;
            ssx += xsx * xsx;
            sax += asa * xsx;
        }

        /* W1 equals (1-W) calculated to avoid excessive rounding error
        for W very near 1 (a potential problem in very large samples) */

        ssassx = sqrt(ssa * ssx);
        w1 = (ssassx - sax) * (ssassx + sax) / (ssa * ssx);
        w = 1.0 - w1;

        pw = significance(samples, w);
    }

    /**
     * Calculate significance level for W
     */
    private static double significance(final double[] samples, final double w) {

        if (samples.length == EXACT_P_SAMPLE_SIZE) { /* exact P value */
            double pw = PI_6 * (asin(sqrt(w)) - STQR);
            if (pw < 0.0) {
                pw = 0.0;
            }
            return pw;
        }

        double y = log(1 - w);
        final double an = (double) samples.length;
        final double mean;
        final double sd;

        if (samples.length <= LOWER_APPROXIMATION_MAX_SAMPLE_SIZE) {
            final double gamma = POLYNOMIAL_FUNCTION_GAMMA.value(an);
            if (y >= gamma) {
                return CLOSE_TO_0; /* an "obvious" value, was 'SMALL' which was 1e-19f */
            }
            y = -log(gamma - y);
            mean = POLYNOMIAL_FUNCTION_3.value(an);
            sd = exp(POLYNOMIAL_FUNCTION_4.value(an));
        } else { /* n >= 12 */
            final double x = log(an);
            mean = POLYNOMIAL_FUNCTION_5.value(x);
            sd = exp(POLYNOMIAL_FUNCTION_6.value(x));
        }

        return new NormalDistribution(mean, sd).probability(y, Double.POSITIVE_INFINITY);
    }

    @Override
    public double statistics() {
        return w;
    }

    @Override
    public double p() {
        return pw;
    }
}
