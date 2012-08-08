package org.asoem.greyfish.utils.math;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("UnusedDeclaration")
public class RandomUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomUtils.class);

    public static final RandomGenerator RANDOM_GENERATOR = new Well19937c();
    public static final RandomData RANDOM_DATA = new RandomDataImpl(RANDOM_GENERATOR);

    /**
     * @return the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence
     * @see java.util.Random#nextDouble()
     */
    public static double nextDouble() {
        return RANDOM_GENERATOR.nextDouble();
    }

    public static int nextInt(final Integer minIncl, final Integer maxExcl) {
        Preconditions.checkArgument(maxExcl >= minIncl);
        return minIncl + RANDOM_GENERATOR.nextInt(maxExcl - minIncl);
    }

    /**
     * Generates a uniformly distributed random value from the open interval (lower,upper) (i.e., endpoints excluded).
     *
     * @param minExcl the lower bound
     * @param maxExcl the upper bound
     * @return a uniformly distributed random value from the open interval (lower,upper)
     */
    public static double nextDouble(double minExcl, double maxExcl) {
        Preconditions.checkArgument(maxExcl >= minExcl);
        return RANDOM_DATA.nextUniform(minExcl, maxExcl);
    }

    public static float nextFloat(float minIncl, float maxExcl) {
        Preconditions.checkArgument(maxExcl >= minIncl);
        return (float) RANDOM_DATA.nextUniform(minIncl, maxExcl);
    }

    public static boolean nextBoolean() {
        return RANDOM_GENERATOR.nextBoolean();
    }

    /**
     * Get a boolean value which is {@code true} with probability {@code p}
     * @param p the probability for {@code true}
     * @return {@code true} with probability {@code p}, false with probability {@code 1-p}
     */
    public static boolean nextBoolean(double p) {
        if (p == 0)
            return false;
        else if (p == 1)
            return true;
        else if (p > 0 && p < 1)
            return nextDouble() < p;
        else
            throw new IllegalArgumentException("Probability not in [0,1]: " + p);
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the specified value (exclusive), drawn from this random number generator's sequence.
     *
     * @param size the bound on the random number to be returned. Must be positive.
     * @return a pseudorandom, uniformly distributed int value between 0 (inclusive) and n (exclusive).
     */
    public static int nextInt(int size) {
        return RANDOM_GENERATOR.nextInt(size);
    }

    /**
     * Generates a random value for the normal distribution with mean equal to {@code mean} and standard deviation equal to {@code sd}.
     *
     * @param mean the mean of the distribution
     * @param sd   the standard deviation of the distribution
     * @return a random value for the given normal distribution
     */
    public static double rnorm(double mean, double sd) {
        double v = 0;
        do {
            RANDOM_DATA.nextGaussian(mean, sd);
        }
        while (Double.isNaN(v)); // bug?
        return v;
    }

    /**
     * Generates a random value for the normal distribution with mean equal to {@code mean} and standard deviation equal to {@code sd}.
     *
     * @param mean the mean for the distribution
     * @return a random value for the given normal distribution
     */
    public static double rpois(double mean) {
        return RANDOM_DATA.nextPoisson(mean);
    }

    /**
     * Generates a random value for the uniform distribution on the interval {@code lower} to {@code upper}.
     *
     * @param lower the lower bound for the distribution
     * @param upper the upper bound for the distribution
     * @return a random value for the given normal distribution
     */
    public static double runif(double lower, double upper) {
        return RANDOM_DATA.nextUniform(lower, upper);
    }

    /**
     * Generates a random value for the uniform distribution on the interval {@code lower} to {@code upper}.
     * Delegates to {@link #runif(double, double)}.
     *
     * @param range the range for the distribution
     * @return a random value for the given normal distribution
     */
    public static double runif(Range<Double> range) {
        return runif(range.lowerEndpoint(), range.upperEndpoint());
    }

    public static <S> S sample(Collection<S> elements) {
        checkNotNull(elements);
        checkArgument(!elements.isEmpty(), "Cannot take a sample out of 0 elements");
        final S sample = Iterables.get(elements, nextInt(elements.size()));
        assert elements.contains(sample);
        LOGGER.debug("Sampled {} out of [{}]", sample, Joiner.on(',').join(elements));
        return sample;
    }

    public static <S> S sample(S... elements) {
        return sample(Arrays.asList(elements));
    }

    public static <S> S sample(S e1, S e2) {
        switch (nextInt(2)) {
            case 0:
                return e1;
            default:
            case 1:
                return e2;
        }
    }

    public static <S> S sample(S e1, S e2, S e3) {
        switch (nextInt(3)) {
            case 0:
                return e1;
            case 1:
                return e2;
            default:
            case 3:
                return e3;
        }
    }

    public static int rbinom(int size, double p) {
        return new BinomialDistribution(size, p).sample();
    }
}
