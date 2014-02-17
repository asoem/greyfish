package org.asoem.greyfish.utils.math;

import com.google.common.base.Supplier;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of common functions using a {@link RandomGenerator}.
 */
public final class RandomGenerators {

    /**
     * Prevent instantiation of this class.
     */
    private RandomGenerators() {
        throw new AssertionError("Not instantiable");
    }

    /**
     * @return a singleton instance of the default {@link RandomGenerator}. This method currently delegates to {@link
     * #well1993c()}
     */
    public static RandomGenerator rng() {
        return well1993c();
    }

    /**
     * @return a singleton instance of a {@link Well19937c} random generator.
     */
    public static RandomGenerator well1993c() {
        return Well19937cHolder.INSTANCE;
    }

    /**
     * Generate a {@code boolean} value which is {@code true} with probability {@code p}.
     *
     * @param rng the generator to use
     * @param p   the probability for generating a {@code true} value
     * @return {@code true} with probability {@code p}, {@code false} otherwise
     */
    public static boolean nextBoolean(final RandomGenerator rng, final double p) {
        return new BinomialDistribution(rng, 1, p).sample() == 1;
    }

    /**
     * Get a random value in the range [{@code minIncl}, {@code maxExcl}).
     *
     * @param rng     the generator to use
     * @param minIncl the minimum inclusive value of the range
     * @param maxExcl the maximum exclusive value of the range
     * @return a random number in [{@code minIncl}, {@code maxExcl})
     */
    public static int nextInt(final RandomGenerator rng, final int minIncl, final int maxExcl) {
        checkNotNull(rng);
        checkArgument(maxExcl >= minIncl);
        return minIncl + rng.nextInt(maxExcl - minIncl);
    }


    /**
     * Generates a random value for the normal distribution with the mean equal to {@code mu} and standard deviation
     * equal to {@code sigma}.
     *
     * @param mu    the mean of the distribution
     * @param sigma the standard deviation of the distribution
     * @return a random value for the given normal distribution
     */
    public static double rnorm(final RandomGenerator rng, final double mu, final double sigma) {
        final NormalDistribution normalDistribution =
                new NormalDistribution(rng, mu, sigma, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        return normalDistribution.sample();
    }

    /**
     * Generates a uniformly distributed random value from the interval [lower,upper).
     *
     * @param rng   the random generator to use
     * @param lower the lower bound
     * @param upper the upper bound
     * @return a uniformly distributed random value from the interval [lower,upper)
     */
    public static double nextDouble(final RandomGenerator rng, final double lower, final double upper) {
        final UniformRealDistribution distribution = new UniformRealDistribution(rng, lower, upper);
        return distribution.sample();
    }

    /**
     * Get an instance of the default random number generator initialized with {@code seed}. Currently delegates to
     * {@link #well1993c(long)}.
     *
     * @param seed the seed for the generator
     * @return a new instance of the default random number generator.
     */
    public static RandomGenerator rng(final long seed) {
        return well1993c(seed);
    }

    private static RandomGenerator well1993c(final long seed) {
        return new Well19937c(seed);
    }

    /**
     * Create a thread safe {@code RandomGenerator} wrapping given {@code randomGenerator}.
     *
     * @param randomGenerator the generator to wrap
     * @return a thread safe {@code RandomGenerator}
     * @see SynchronizedRandomGenerator
     */
    public static RandomGenerator synchronizedGenerator(final RandomGenerator randomGenerator) {
        if (randomGenerator instanceof SynchronizedRandomGenerator) {
            return randomGenerator;
        } else {
            return new SynchronizedRandomGenerator(randomGenerator);
        }
    }

    private static class Well19937cHolder implements Serializable {

        private static final Well19937c INSTANCE = new Well19937c();

        // preserving singleton-ness gives equals()/hashCode() for free
        private Object readResolve() {
            return INSTANCE;
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Create a {@code RandomGenerator} which delegates all methods to a thread local instance of a generator created by
     * the given {@code generatorSupplier}.
     *
     * @param generatorSupplier the factory to create the thread local instances
     * @return a new generator delegating to a thread local instance
     * @see ThreadLocal
     */
    public static RandomGenerator threadLocalGenerator(final Supplier<RandomGenerator> generatorSupplier) {
        checkNotNull(generatorSupplier);
        return new ThreadLocalRandomGenerator(generatorSupplier);
    }

    private static class ThreadLocalRandomGenerator extends ForwardingRandomGenerator implements Serializable {

        private final transient ThreadLocal<RandomGenerator> localRandom = new ThreadLocal<RandomGenerator>() {
            @Override
            protected RandomGenerator initialValue() {
                return generatorSupplier.get();
            }
        };
        private final Supplier<? extends RandomGenerator> generatorSupplier;

        public ThreadLocalRandomGenerator(final Supplier<? extends RandomGenerator> generatorSupplier) {
            this.generatorSupplier = generatorSupplier;
        }

        @Override
        protected RandomGenerator delegate() {
            return localRandom.get();
        }

        private static final long serialVersionUID = 0;
    }
}
