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

package org.asoem.greyfish.utils.math.statistics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.*;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.exception.NotFiniteNumberException;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Tuple2;
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
     * fitnessFunction}. <p>This scheme is also known as stochastic sampling with replacement and therefore equivalent
     * to {@code Samplings.stochastic(fitnessFunction, rng).withReplacement()}.</p>
     *
     *
     * @param fitnessFunction the function to compute the fitness of the elements
     * @param rng             the random generator
     * @return the strategy to select k elements by using a roulette wheel
     */
    public static <E> Sampling<E> rouletteWheelSelection(
            final Function<? super E, Double> fitnessFunction, final RandomGenerator rng) {
        return stochastic(fitnessFunction, rng).withReplacement();
    }

    public static <E> ReplacementVariants<E> stochastic(
            final Function<? super E, Double> fitnessFunction, final RandomGenerator rng) {
        return new ReplacementVariants<E>() {
            @Override
            public Sampling<E> withReplacement() {
                return new RouletteWheelSelection<>(fitnessFunction, rng);
            }

            @Override
            public Sampling<E> withoutReplacement() {
                throw new UnsupportedOperationException("Not implemented");
            }
        };
    }

    /**
     * A the selection strategy for which the {@link Sampling#sample(java.util.Collection, int)} method returns the
     * {@code k} best elements out of {@code n} elements.
     *
     * @return the strategy to select the k "best" elements of a collection
     */
    public static Sampling<Comparable<?>> elitistSelection() {
        return elitistSelection(Ordering.natural());
    }

    public static <T> Sampling<T> elitistSelection(final Ordering<? super T> ordering) {
        return new ElitistSelection<>(ordering);
    }

    @VisibleForTesting
    static class ElitistSelection<E> implements Sampling<E> {

        private final Ordering<? super E> ordering;

        public ElitistSelection(final Ordering<? super E> ordering) {
            this.ordering = ordering;
        }

        @Override
        public <T extends E> Iterable<T> sample(final Collection<? extends T> elements, final int k) {
            return ordering.greatestOf(Collections.unmodifiableCollection(elements), k);
        }
    }

    public static ReplacementVariants<Object> random(final RandomGenerator rng) {
        return new ReplacementVariants<Object>() {
            @Override
            public Sampling<Object> withReplacement() {
                return new RandomSelectionWithReplacement(rng);
            }

            @Override
            public Sampling<Object> withoutReplacement() {
                return new RandomSelectionWithoutReplacement(rng);
            }
        };
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

    /**
     * This sampling scheme returns {@code k} times the the best element, which is the one with the highest rank defined
     * by the natural ordering of the elements.
     *
     * @return a sampling scheme returning the "best" element of a collection
     * @see com.google.common.collect.Ordering#natural()
     */
    public static Sampling<Comparable<?>> bestSelection() {
        return bestSelection(Ordering.natural());
    }

    public static <T> Sampling<T> bestSelection(final Ordering<? super T> ordering) {
        return new BestSelection<>(ordering);
    }

    @VisibleForTesting
    static class BestSelection<T> implements Sampling<T> {

        private final Ordering<? super T> ordering;

        BestSelection(final Ordering<? super T> ordering) {
            this.ordering = ordering;
        }

        @Override
        public <E extends T> Iterable<E> sample(final Collection<? extends E> elements, final int k) {
            final E best = ordering.max(elements);
            return Collections.nCopies(k, best);
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
                if (!Doubles.isFinite(fitness)) {
                    throw new NotFiniteNumberException(fitness);
                }
                elementFitnessTupleList.add(Tuple2.of(element, fitness));
                cumulativeFitness += fitness;
            }

            if (cumulativeFitness == 0.0) {
                return random(rng).withReplacement().sample(elements, k);
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
                            throw new AssertionError(String.format("Out of elements for cf=%s, rand=%s, sum=%s",
                                    finalCumulativeFitness, rand, sum));
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
                    "Cannot sample %s elements without replacement from a collection of size %s", k, elements.size());

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
