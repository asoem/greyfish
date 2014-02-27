package org.asoem.greyfish.utils.collect;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.math.RandomGenerators;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of common {@link Sampling} implementations.
 */
public final class Samplings {

    private Samplings() {
        throw new UnsupportedOperationException();
    }

    /**
     * A selection scheme which samples elements proportionate to their fitness defined by the given {@code
     * fitnessFunction}. <p>This scheme is also known as stochastic sampling with replacement.</p>
     *
     * @param fitnessFunction the function to compute the fitness of the elements
     * @param rng             the random generator
     * @return the strategy to select k elements by using a roulette wheel
     */
    public static <E> Sampling<E> rouletteWheelSelection(
            final Function<E, Double> fitnessFunction, final RandomGenerator rng) {
        return new RouletteWheelSelection<>(fitnessFunction, rng);
    }

    /**
     * A the selection strategy for which the {@link Sampling#sample(java.util.Collection, int)} method returns the
     * {@code k} best elements out of {@code n} elements.
     *
     * @return the strategy to select the k "best" elements of a collection
     */
    public static Sampling<Comparable<?>> elitistSelection() {
        return ElitistSelection.INSTANCE;
    }

    @VisibleForTesting
    enum ElitistSelection implements Sampling<Comparable<?>> {
        INSTANCE;

        @Override
        public <T extends Comparable<?>> Iterable<T> sample(final Collection<? extends T> elements, final int k) {
            return Ordering.natural().greatestOf(Collections.unmodifiableCollection(elements), k);
        }
    }

    /**
     * A the selection strategy for which the {@link Sampling#sample(java.util.Collection, int)} method returns {@code
     * k} random elements out of {@code n} elements.
     *
     * @param rng the random generator to use
     * @return a strategy to select k random elements
     */
    public static Sampling<Object> randomWithReplacement(final RandomGenerator rng) {
        return new RandomSelectionWithReplacement(rng);
    }

    @VisibleForTesting
    static class RandomSelectionWithReplacement implements Sampling<Object> {
        private final RandomGenerator rng;

        RandomSelectionWithReplacement(final RandomGenerator rng) {
            this.rng = rng;
        }

        @Override
        public <T> Iterable<T> sample(final Collection<? extends T> elements, final int k) {
            checkNotNull(rng);
            checkNotNull(elements);

            if (k == 0) {
                return ImmutableList.of();
            }

            switch (elements.size()) {
                case 0:
                    throw new IllegalArgumentException("Cannot sample element from empty list");
                case 1:
                    final T onlyElement = Iterables.getOnlyElement(elements);
                    final ImmutableList.Builder<T> builder = ImmutableList.builder();
                    for (int i = 0; i < k; i++) {
                        builder.add(onlyElement);
                    }
                    return builder.build();
                default:
                    final UniformIntegerDistribution distribution =
                            new UniformIntegerDistribution(rng, 0, elements.size() - 1);
                    final int[] randomIndexes = distribution.sample(k);
                    assert randomIndexes.length == k;
                    final List<T> list = new ArrayList<>(k);
                    for (int randomIndex : randomIndexes) {
                        list.add(Iterables.get(elements, randomIndex));
                    }
                    return ImmutableList.copyOf(list);
            }
        }
    }

    public static Sampling<Object> randomWithoutReplacement(final RandomGenerator rng) {
        return new RandomSelectionWithoutReplacement(rng);
    }

    /**
     * A the selection strategy for which the {@link Sampling#sample(java.util.Collection, int)} method returns {@code
     * k} times the the best element, which is the one with the highest value defined by the elements {@code compare}
     * method.
     *
     * @return the strategy to get the "best" element of a collection
     */
    public static Sampling<Comparable<?>> bestSelection() {
        return BestSelection.INSTANCE;
    }

    @VisibleForTesting
    enum BestSelection implements Sampling<Comparable<?>> {
        INSTANCE;

        @Override
        public <T extends Comparable<?>> Iterable<T> sample(final Collection<? extends T> elements, final int k) {
            final T best = Ordering.natural().max(elements);
            return new OneForAllList<>(best, k);
        }

        private static class OneForAllList<T extends Comparable<?>> extends AbstractList<T> {
            private final T element;
            private final int k;

            private OneForAllList(final T element, final int k) {
                this.element = element;
                this.k = k;
            }

            @Override
            public T get(final int index) {
                return element;
            }

            @Override
            public int size() {
                return k;
            }
        }
    }

    @VisibleForTesting
    static class RouletteWheelSelection<E> implements Sampling<E> {

        private final Function<? super E, ? extends Double> function;
        private final RandomGenerator rng;

        public RouletteWheelSelection(
                final Function<? super E, ? extends Double> function, final RandomGenerator rng) {
            this.function = function;
            this.rng = rng;
        }

        @Override
        public <T extends E> Iterable<T> sample(final Collection<? extends T> elements, final int k) {
            checkNotNull(elements, "elements");
            checkArgument(elements.size() >= k, "%s < %s", elements.size(), k);
            checkArgument(k >= 0, "%s < 0", k);

            Double cumulativeFitness = 0.0;
            final List<Product2<T, Double>> elementFitnessTupleList = Lists.newArrayList();
            for (T element : elements) {
                final Double fitness = checkNotNull(function.apply(element));
                elementFitnessTupleList.add(Tuple2.of(element, fitness));
                cumulativeFitness += fitness;
            }

            if (cumulativeFitness == 0.0) {
                return randomWithReplacement(RandomGenerators.rng()).sample(elements, k);
            }

            final Double finalCumulativeFitness = cumulativeFitness;
            return Iterables.limit(new Iterable<T>() {
                @Override
                public Iterator<T> iterator() {
                    return new AbstractIterator<T>() {

                        @Override
                        protected T computeNext() {
                            final double rand = RandomGenerators.nextDouble(rng, 0, finalCumulativeFitness);
                            double sum = 0.0;
                            for (final Product2<T, Double> element : elementFitnessTupleList) {
                                sum += element.second();
                                if (rand < sum) {
                                    return element.first();
                                }
                            }
                            throw new AssertionError();
                        }
                    };
                }
            }, k);
        }
    }

    @VisibleForTesting
    static class RandomSelectionWithoutReplacement implements Sampling<Object> {
        private final RandomGenerator rng;

        public RandomSelectionWithoutReplacement(final RandomGenerator rng) {
            this.rng = rng;
        }

        @Override
        public <T> Iterable<T> sample(final Collection<? extends T> elements, final int k) {
            checkNotNull(rng);
            checkNotNull(elements);
            checkArgument(!elements.isEmpty(),
                    "Cannot sample element from empty collection");
            checkArgument(k <= elements.size(),
                    "Cannot sample {} unique elements from collection of size {}", k, elements.size());

            if (k == 0) {
                return ImmutableList.of();
            } else if (k / (double) elements.size() < 0.1) {
                return sampleUniqueUsingIndexSet(elements, k, rng);
            } else {
                return sampleUniqueUsingShuffle(elements, k, rng);
            }
        }

        private static <T> Iterable<T> sampleUniqueUsingIndexSet(final Collection<? extends T> collection,
                                                                 final int n, final RandomGenerator rng) {
            final UniformIntegerDistribution distribution =
                    new UniformIntegerDistribution(rng, 0, collection.size() - 1);
            final LinkedHashSet<Integer> indexSet = Sets.newLinkedHashSet();
            while (indexSet.size() != n) {
                indexSet.add(distribution.sample());
            }

            return Iterables.transform(indexSet, new Function<Integer, T>() {
                @Nullable
                @Override
                public T apply(final Integer input) {
                    return Iterables.get(collection, input);
                }
            });
        }

        private static <T> Iterable<T> sampleUniqueUsingShuffle(final Iterable<? extends T> elements, final int n,
                                                                final RandomGenerator rng) {
            final List<T> in = Lists.newArrayList(elements);
            Collections.shuffle(in, new RandomAdaptor(rng));
            return in.subList(0, n);
        }
    }
}
