package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.asoem.greyfish.utils.math.RandomGenerators;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of common {@link ElementSelectionStrategy} implementations.
 */
public final class ElementSelectionStrategies {

    private ElementSelectionStrategies() {}

    /**
     * A the selection strategy for which the {@link ElementSelectionStrategy#pick(java.util.List, int)} method returns
     * the {@code k} elements out of {@code n} elements by using a roulette wheel approach.
     *
     * @param <E>      the comparable type of the elements
     * @param function the function to compute the fitness of the elements
     * @return the strategy to select k elements by using a roulette wheel
     */
    public static <E> ElementSelectionStrategy<E> rouletteWheelSelection(final Function<E, ? extends Double> function) {
        return new ElementSelectionStrategy<E>() {

            @Override
            public <T extends E> Iterable<T> pick(final List<? extends T> elements, final int k) {
                final double fitness = cumulativeFitness(elements);
                if (fitness == 0) {
                    return randomSelection().pick(elements, k);
                }

                return Iterables.limit(new Iterable<T>() {
                    @Override
                    public Iterator<T> iterator() {
                        return new AbstractIterator<T>() {

                            @Override
                            protected T computeNext() {
                                final double rand = RandomGenerators.nextDouble(RandomGenerators.rng(), 0, fitness);
                                double sum = 0.0;
                                for (final T element : elements) {
                                    final Double fitness = checkNotNull(function.apply(element));
                                    sum += fitness;
                                    if (rand < sum) {
                                        return element;
                                    }
                                }
                                throw new AssertionError();
                            }
                        };
                    }
                }, k);
            }

            private double cumulativeFitness(final Iterable<? extends E> elements) {
                double sum = 0.0;
                for (final E element : elements) {
                    sum += checkNotNull(function.apply(element));
                }

                return sum;
            }
        };
    }

    /**
     * A the selection strategy for which the {@link ElementSelectionStrategy#pick(java.util.List, int)} method returns
     * the {@code k} best elements out of {@code n} elements.
     *
     * @param <E> the comparable type of the elements
     * @return the strategy to select the k "best" elements of a collection
     */
    @SuppressWarnings("unchecked") // ElitistSelection.INSTANCE is invariant of the type
    public static <E extends Comparable<?>> ElementSelectionStrategy<E> elitistSelection() {
        return (ElementSelectionStrategy<E>) ElitistSelection.INSTANCE;
    }

    private enum ElitistSelection implements ElementSelectionStrategy<Comparable<?>> {
        INSTANCE;

        @Override
        public <T extends Comparable<?>> Iterable<T> pick(final List<? extends T> elements, final int k) {
            return Ordering.natural().greatestOf(Collections.unmodifiableList(elements), k);
        }
    }

    /**
     * A the selection strategy for which the {@link ElementSelectionStrategy#pick(java.util.List, int)} method returns
     * {@code k} random elements out of {@code n} elements.
     *
     * @param <E> the comparable type of the elements
     * @return a strategy to select k random elements
     */
    @SuppressWarnings("unchecked") // RandomSelection.INSTANCE is invariant of the type
    public static <E> ElementSelectionStrategy<E> randomSelection() {
        return (ElementSelectionStrategy<E>) RandomSelection.INSTANCE;
    }

    private enum RandomSelection implements ElementSelectionStrategy<Object> {
        INSTANCE;

        @Override
        public <T> Iterable<T> pick(final List<? extends T> elements, final int k) {
            checkNotNull(elements);
            checkArgument(k >= 0);
            switch (k) {
                case 0:
                    return Collections.emptyList();
                default:
                    return RandomGenerators.sample(elements, k, RandomGenerators.rng());
            }
        }
    }

    /**
     * A the selection strategy for which the {@link ElementSelectionStrategy#pick(java.util.List, int)} method returns
     * {@code k} times the the best element, which is the one with the highest value defined by the elements {@code
     * compare} method.
     *
     * @param <E> the comparable type of the elements
     * @return the strategy to get the "best" element of a collection
     */
    @SuppressWarnings("unchecked") // BestSelection.INSTANCE is independent of any type
    public static <E extends Comparable<?>> ElementSelectionStrategy<E> bestSelection() {
        return (ElementSelectionStrategy<E>) BestSelection.INSTANCE;
    }

    private enum BestSelection implements ElementSelectionStrategy<Comparable<?>> {
        INSTANCE;

        @Override
        public <T extends Comparable<?>> Iterable<T> pick(final List<? extends T> elements, final int k) {
            final T best = Ordering.natural().max(elements);
            return new OneForAllList<T>(best, k);
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
}
