package org.asoem.greyfish.utils.math.distribution;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;

public abstract class AbstractIntegerDistribution implements IntegerDistribution, Serializable {

    /**
     * Serializable version identifier
     */
    private static final long serialVersionUID = -1146319659338487221L;

    /**
     * RNG instance used to generate samples from the distribution.
     *
     * @since 3.1
     */
    private final RandomGenerator random;

    /**
     * @deprecated As of 3.1, to be removed in 4.0. Please use {@link #AbstractIntegerDistribution(RandomGenerator)}
     * instead.
     */
    @Deprecated
    protected AbstractIntegerDistribution() {
        // Legacy users are only allowed to access the deprecated "randomData".
        // New users are forbidden to use this constructor.
        random = null;
    }

    /**
     * @param rng Random number generator.
     * @since 3.1
     */
    protected AbstractIntegerDistribution(final RandomGenerator rng) {
        random = rng;
    }

    /**
     * {@inheritDoc} <p/> The default implementation uses the identity <p>{@code P(x0 < X <= x1) = P(X <= x1) - P(X <=
     * x0)}</p>
     */
    public final double cumulativeProbability(final int x0, final int x1) {
        if (x1 < x0) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_ENDPOINT_ABOVE_UPPER_ENDPOINT,
                    x0, x1, true);
        }
        return cumulativeProbability(x1) - cumulativeProbability(x0);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The default implementation returns <ul> <li>{@link #getSupportLowerBound()} for {@code p = 0},</li> <li>{@link
     * #getSupportUpperBound()} for {@code p = 1}, and</li> <li>{@link #solveInverseCumulativeProbability(double, int,
     * int)} for {@code 0 < p < 1}.</li> </ul>
     */
    public final int inverseCumulativeProbability(final double p) {
        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0, 1);
        }

        int lower = getSupportLowerBound();
        if (p == 0.0) {
            return lower;
        }
        if (lower == Integer.MIN_VALUE) {
            if (checkedCumulativeProbability(lower) >= p) {
                return lower;
            }
        } else {
            lower -= 1; // this ensures cumulativeProbability(lower) < p, which
            // is important for the solving step
        }

        int upper = getSupportUpperBound();
        if (p == 1.0) {
            return upper;
        }

        /*
        // use the one-sided Chebyshev inequality to narrow the bracket
        // cf. AbstractRealDistribution.inverseCumulativeProbability(double)
        final double mu = getNumericalMean();
        final double sigma = FastMath.sqrt(getNumericalVariance());
        final boolean chebyshevApplies = !(Double.isInfinite(mu) || Double.isNaN(mu) ||
                Double.isInfinite(sigma) || Double.isNaN(sigma) || sigma == 0.0);
        if (chebyshevApplies) {
            double k = FastMath.sqrt((1.0 - p) / p);
            double tmp = mu - k * sigma;
            if (tmp > lower) {
                lower = ((int) Math.ceil(tmp)) - 1;
            }
            k = 1.0 / k;
            tmp = mu + k * sigma;
            if (tmp < upper) {
                upper = ((int) Math.ceil(tmp)) - 1;
            }
        }
        */
        return solveInverseCumulativeProbability(p, lower, upper);
    }

    /**
     * This is a utility function used by {@link #inverseCumulativeProbability(double)}. It assumes {@code 0 < p < 1}
     * and that the inverse cumulative probability lies in the bracket <code>(lower, upper]</code>. The implementation
     * does simple bisection to find the smallest {@code p}-quantile <code>inf{x in Z | P(X<=x) >= p}</code>.
     *
     * @param p     the cumulative probability
     * @param lower a value satisfying {@code cumulativeProbability(lower) < p}
     * @param upper a value satisfying {@code p <= cumulativeProbability(upper)}
     * @return the smallest {@code p}-quantile of this distribution
     */
    protected final int solveInverseCumulativeProbability(final double p, final int lower, final int upper) {
        int adjustedLower = lower;
        int adjustedUpper = upper;

        while (adjustedLower + 1 < adjustedUpper) {
            int xm = (adjustedLower + adjustedUpper) / 2;
            if (xm < adjustedLower || xm > adjustedUpper) {
                /*
                 * Overflow.
                 * There will never be an overflow in both calculation methods
                 * for xm at the same time
                 */
                xm = adjustedLower + (adjustedUpper - adjustedLower) / 2;
            }

            double pm = checkedCumulativeProbability(xm);
            if (pm >= p) {
                adjustedUpper = xm;
            } else {
                adjustedLower = xm;
            }
        }
        return upper;
    }

    /**
     * {@inheritDoc}
     */
    public final void reseedRandomGenerator(final long seed) {
        random.setSeed(seed);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The default implementation uses the <a href="http://en.wikipedia.org/wiki/Inverse_transform_sampling"> inversion
     * method</a>.
     */
    public final int sample() {
        return inverseCumulativeProbability(random.nextDouble());
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The default implementation generates the sample by calling {@link #sample()} in a loop.
     */
    public final int[] sample(final int sampleSize) {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(
                    LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
        }
        int[] out = new int[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            out[i] = sample();
        }
        return out;
    }

    /**
     * Computes the cumulative probability function and checks for {@code NaN} values returned. Throws {@code
     * MathInternalError} if the value is {@code NaN}. Rethrows any exception encountered evaluating the cumulative
     * probability function. Throws {@code MathInternalError} if the cumulative probability function returns {@code
     * NaN}.
     *
     * @param argument input value
     * @return the cumulative probability
     * @throws org.apache.commons.math3.exception.MathInternalError if the cumulative probability is {@code NaN}
     */
    private double checkedCumulativeProbability(final int argument) {
        double result = cumulativeProbability(argument);
        if (Double.isNaN(result)) {
            throw new MathInternalError(LocalizedFormats
                    .DISCRETE_CUMULATIVE_PROBABILITY_RETURNED_NAN, argument);
        }
        return result;
    }
}
