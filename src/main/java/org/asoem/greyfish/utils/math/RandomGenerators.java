package org.asoem.greyfish.utils.math;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 29.05.13
 * Time: 11:47
 *
 * Singleton Instances of some default generators
 */
public class RandomGenerators {

    private RandomGenerators() {}

    /**
     *
     * @return a singleton instance of the default {@link RandomGenerator}.
     * This method currently delegates to {@code #well1993c}
     */
    public static RandomGenerator rng() {
        return well1993c();
    }

    /**
     *
     * @return a singleton instance of {@link Well19937c}
     */
    public static RandomGenerator well1993c() {
        return Well1993cSingleton.INSTANCE;
    }

    /**
     * Generate a {@code boolean} value which is {@code true} with probability {@code p}.
     * @param rng the generator to use
     * @param p the probability for generating a {@code true} value
     * @return {@code true} with probability {@code p}, {@code false} otherwise
     */
    public static boolean nextBoolean(RandomGenerator rng, double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 || p <= 1, "{} is not in [0,1]", p);
        final double v = rng.nextDouble();
        return v < p;
    }

    public static <S> S sample(RandomGenerator rng, S e1, S e2) {
        return rng.nextBoolean() ? e1 : e2;
    }

    public static int nextInt(RandomGenerator rng, final Integer minIncl, final Integer maxExcl) {
        checkNotNull(rng);
        checkArgument(maxExcl >= minIncl);
        return minIncl + rng.nextInt(maxExcl - minIncl);
    }

    @Nullable
    public static <T> T sample(RandomGenerator rng, List<T> elements) {
        checkNotNull(rng);
        checkNotNull(elements);
        return elements.get(rng.nextInt(elements.size()));
    }

    /**
     * Generates a random value for the normal distribution with the mean equal to {@code mu} and standard deviation equal to {@code sigma}.
     *
     * @param mu the mean of the distribution
     * @param sigma the standard deviation of the distribution
     * @return a random value for the given normal distribution
     */
    public static double rnorm(RandomGenerator rng, double mu, double sigma) {
        checkNotNull(rng);
        checkArgument(sigma > 0, "Sigma must be strictly positive, was: %s", sigma);
        final double gaussian = rng.nextGaussian();
        return sigma * gaussian + mu;
    }

    /**
     * Generates a uniformly distributed random value from the interval [lower,upper).
     *
     * @param rng the {@code RandomGenerator to use}
     * @param lower the lower bound
     * @param upper the upper bound
     * @return a uniformly distributed random value from the open interval (lower,upper)
     */
    public static double nextDouble(RandomGenerator rng, double lower, double upper) {
        return nextDouble(rng, lower, upper, true);
    }

    private static double nextDouble(RandomGenerator rng, double lower, double upper, boolean lowerInclusive) {

        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                    lower, upper, false);
        }

        if (Double.isInfinite(lower) || Double.isInfinite(upper)) {
            throw new MathIllegalArgumentException(LocalizedFormats.INFINITE_BOUND);
        }

        if (Double.isNaN(lower) || Double.isNaN(upper)) {
            throw new MathIllegalArgumentException(LocalizedFormats.NAN_NOT_ALLOWED);
        }

        // ensure nextDouble() isn't 0.0
        double u = rng.nextDouble();
        while (!lowerInclusive && u <= 0.0) {
            u = rng.nextDouble();
        }

        return u * upper + (1.0 - u) * lower;
    }

    private static class Well1993cSingleton extends RandomAdaptor implements Serializable {

        private static final Well1993cSingleton INSTANCE = new Well1993cSingleton();

        private Well1993cSingleton() {
            super(new Well19937c());
        }

        // preserving singleton-ness gives equals()/hashCode() for free
        private Object readResolve() {
            return INSTANCE;
        }

        private static final long serialVersionUID = 0;
    }
}
