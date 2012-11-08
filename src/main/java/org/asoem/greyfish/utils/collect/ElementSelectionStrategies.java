package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.asoem.greyfish.utils.math.RandomUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 12:56
 */
public final class ElementSelectionStrategies {

    private ElementSelectionStrategies() {}

    public static <E> ElementSelectionStrategy<E> rouletteWheelSelection(final Function<E, ? extends Double> function) {
        return new ElementSelectionStrategy<E>() {

            @Override
            public <T extends E> Iterable<T> pick(final List<? extends T> elements, int k) {
                final double f_sum = cumulative_fitness(elements);
                if (f_sum == 0)
                    return randomSelection().pick(elements, k);

                return Iterables.limit(new Iterable<T>() {
                    @Override
                    public Iterator<T> iterator() {
                        return new AbstractIterator<T>() {

                            @Override
                            protected T computeNext() {
                                final double rand = RandomUtils.nextDouble(0,f_sum);
                                Double step_sum = 0.0;
                                for (T element : elements) {
                                    step_sum += function.apply(element);
                                    if (rand < step_sum)
                                        return element;
                                }
                                throw new AssertionError();
                            }
                        };
                    }
                }, k);
            }

            private double cumulative_fitness(Iterable<? extends E> elements) {
                Double sum = 0.0;
                for (E element : elements)
                    sum += function.apply(element);

                return sum;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <E extends Comparable<?>> ElementSelectionStrategy<E> elitistSelection() {
        return (ElementSelectionStrategy<E>) ElitistSelection.INSTANCE;
    }

    private enum ElitistSelection implements ElementSelectionStrategy<Comparable<?>> {
        INSTANCE;

        @Override
        public <T extends Comparable<?>> Iterable<T> pick(List<? extends T> elements, int k) {
            return Ordering.natural().greatestOf(Collections.unmodifiableList(elements), k);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> ElementSelectionStrategy<E> randomSelection() {
        return (ElementSelectionStrategy<E>) RandomSelection.INSTANCE;
    }

    private enum RandomSelection implements ElementSelectionStrategy<Object> {
        INSTANCE;

        @Override
        public <T> Iterable<T> pick(final List<? extends T> elements, int k) {
            checkNotNull(elements);
            checkArgument(k >= 0);
            switch (k) {
                case 0:
                    return Collections.emptyList();
                default:
                    return Iterables.limit(new Iterable<T>() {
                        @Override
                        public Iterator<T> iterator() {
                            return new AbstractIterator<T>() {

                                @Override
                                protected T computeNext() {
                                    return elements.get(RandomUtils.nextInt(elements.size()));
                                }
                            };
                        }
                    }, k);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Comparable<?>> ElementSelectionStrategy<E> bestSelection() {
        return (ElementSelectionStrategy<E>) BestSelection.INSTANCE;
    }

    private enum BestSelection implements ElementSelectionStrategy<Comparable<?>> {
        INSTANCE;

        @Override
        public <T extends Comparable<?>> Iterable<T> pick(List<? extends T> elements, int k) {
            return Iterables.limit(Iterables.cycle(Collections.singleton(Ordering.natural().max(elements))), k);
        }
    }
}
