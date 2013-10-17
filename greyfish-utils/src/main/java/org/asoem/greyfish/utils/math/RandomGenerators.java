package org.asoem.greyfish.utils.math;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.*;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

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
        return Well19937cHolder.INSTANCE;
    }

    /**
     * Generate a {@code boolean} value which is {@code true} with probability {@code p}.
     * @param rng the generator to use
     * @param p the probability for generating a {@code true} value
     * @return {@code true} with probability {@code p}, {@code false} otherwise
     */
    public static boolean nextBoolean(final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 || p <= 1, "{} is not in [0,1]", p);
        final double v = rng.nextDouble();
        return v < p;
    }

    /**
     * Randomly sample one element out of {@code e1} and {@code e2}.
     *
     * @param rng the generator to use for sampling
     * @param e1 the first element to sample from
     * @param e2 the second element to sample from
     * @param <S> the type of the elements to sample
     * @return {@code e1} or {@code e2}
     */
    public static <S> S sample(final RandomGenerator rng, final S e1, final S e2) {
        return rng.nextBoolean() ? e1 : e2;
    }

    /**
     * Get a random value in the range [{@code minIncl}, {@code maxExcl}).
     * @param rng the generator to use
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
     * Randomly sample one element from given {@code elements} using {@code rng}.
     *
     * @param rng the random number generator to use
     * @param elements the elements to sample
     * @param <T> the type of the elements to sample
     * @return the sampled element
     */
    @Nullable
    public static <T> T sample(final RandomGenerator rng, final Collection<? extends T> elements) {
        checkNotNull(rng);
        checkNotNull(elements);
        checkArgument(!elements.isEmpty(), "Cannot sample element from empty list");
        return Iterables.get(elements, rng.nextInt(elements.size()));
    }

    /**
     * <p>Randomly sample {@code sampleSize} elements from given {@code collection} using {@code rng}.<br>
     * Each element will be contained in the returned {@code Iterable} exactly once.<br>
     * {@code sampleSize} must be less than or equal to the size of {@code collection}.</p>
     *
     * <p><b>Attention: Although the elements are selected randomly, the order of the returned elements is NOT random.
     * You should shuffle the the returned elements if this is not acceptable.</b>
     * </p>
     *
     * @param rng the random number generator to use
     * @param collection the collection to sample from
     * @param sampleSize the number of elements to sample
     * @return an unmodifiable iterable containing randomly but not repeatedly selected elements of the collection
     */
    public static <T> Iterable<T> sampleOnce(final RandomGenerator rng,
                                             final Collection<? extends T> collection, final int sampleSize) {
        checkNotNull(rng);
        checkNotNull(collection);
        checkArgument(!collection.isEmpty(),
                "Cannot sample element from empty collection");
        checkArgument(sampleSize <= collection.size(),
                "Cannot sample {} unique elements from collection of size {}", sampleSize, collection.size());

        final SortedSet<Integer> indexSet = Sets.newTreeSet();
        while (indexSet.size() != sampleSize) {
            indexSet.add(rng.nextInt(collection.size()));
        }

        if (collection instanceof List && collection instanceof RandomAccess) {
            @SuppressWarnings("unchecked") // checked by instanceof
            final List<T> elementList = (List<T>) collection;
            return filterByIndex(elementList, indexSet);
        } else {
            return filterByStep(collection, indexSet);
        }
    }

    private static <T> Iterable<T> filterByIndex(final List<T> elements, final Set<Integer> indexes) {
        return Iterables.transform(indexes, new Function<Integer, T>() {
            @Override
            public T apply(final Integer input) {
                return elements.get(input);
            }
        });
    }

    private static <T> FluentIterable<T> filterByStep(final Iterable<? extends T> elements, final SortedSet<Integer> indexes) {
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new AbstractIterator<T>() {
                    private int index = -1;
                    private Iterator<Integer> indexIterator = indexes.iterator();
                    private Iterator<? extends T> elementIterator = elements.iterator();

                    @Override
                    protected T computeNext() {
                        if (indexIterator.hasNext()) {
                            final Integer nextIndex = indexIterator.next();
                            assert nextIndex > index;

                            T next = null;
                            while (index != nextIndex && elementIterator.hasNext()) {
                                next = elementIterator.next();
                                index++;
                            }

                            return next;
                        } else {
                            return endOfData();
                        }
                    }
                };
            }
        };
    }

    /**
     * Randomly sample {@code sampleSize} elements from given {@code elements} using {@code rng}.
     *
     * @param rng the random number generator to use
     * @param elements the elements to sample
     * @param sampleSize the number of elements to sample
     * @param <T> the type of the elements to sample
     * @return the collection of sampled elements
     */
    public static <T> Collection<T> sample(final RandomGenerator rng,
                                           final Collection<? extends T> elements, final int sampleSize) {
        checkNotNull(rng);
        checkNotNull(elements);
        checkArgument(!elements.isEmpty(), "Cannot sample element from empty list");
        final List<T> list = new ArrayList<T>(sampleSize);
        for (int i = 0; i < sampleSize; i++) {
            list.add(sample(rng, elements));
        }
        return ImmutableList.copyOf(list);
    }

    /**
     * Generates a random value for the normal distribution
     * with the mean equal to {@code mu} and standard deviation equal to {@code sigma}.
     *
     * @param mu the mean of the distribution
     * @param sigma the standard deviation of the distribution
     * @return a random value for the given normal distribution
     */
    public static double rnorm(final RandomGenerator rng, final double mu, final double sigma) {
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
    public static double nextDouble(final RandomGenerator rng, final double lower, final double upper) {
        return nextDouble(rng, lower, upper, true);
    }

    private static double nextDouble(final RandomGenerator rng, final double lower,
                                     final double upper, final boolean lowerInclusive) {

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

    /**
     * Get an instance of the default random number generator initialized with {@code seed}.
     * Currently delegates to {@link #well1993c(long)}.
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
     * Create a {@code RandomGenerator} which delegates all methods to a thread local instance
     * of a generator created by the given {@code generatorSupplier}.
     * @param generatorSupplier the factory to create the thread local instances
     * @return a new generator delegating to a thread local instance
     * @see ThreadLocal
     */
    public static RandomGenerator threadLocalGenerator(final Supplier<RandomGenerator> generatorSupplier) {
        checkNotNull(generatorSupplier);
        return new ThreadLocalRandomGenerator() {

            @Override
            protected RandomGenerator createGenerator() {
                return generatorSupplier.get();
            }
        };
    }

    private abstract static class ThreadLocalRandomGenerator extends ForwardingRandomGenerator {

        private final ThreadLocal<RandomGenerator> localRandom = new ThreadLocal<RandomGenerator>() {
            @Override
            protected RandomGenerator initialValue() {
                return createGenerator();
            }
        };

        protected abstract RandomGenerator createGenerator();

        @Override
        protected RandomGenerator delegate() {
            return localRandom.get();
        }
    }
}
