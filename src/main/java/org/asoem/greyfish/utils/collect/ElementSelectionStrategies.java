package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicates;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 12:56
 */
public class ElementSelectionStrategies {
    /**
     * Get {@code n} elements of given {@code elements} which are chosen by the given {@code strategy}. The picked elements can be returned multiple times.
     * @param elements The elements to choose from
     * @param strategy The strategy to choose the elements
     * @param n The number of elements to pick
     * @param <E> The type of the elements to pick
     * @return An Iterable over the picked elements
     */
    public static <E> Iterable<E> pickAndPutBack(final Iterable<? extends E> elements, final ElementSelectionStrategy<? super E> strategy, final int n) {
        return new Iterable<E>() {
            @Override
            public Iterator<E> iterator() {
                return new AbstractIterator<E>() {
                    private int counter = n;
                    @Override
                    protected E computeNext() {
                        return (counter-- > 0) ? strategy.pick(elements) : endOfData();
                    }
                };
            }
        };
    }

    /**
     * Get {@code n} elements of given {@code elements} which are chosen by the given {@code strategy}. Each element can be picked just once.
     * @param elements The elements to choose from
     * @param strategy The strategy to choose the elements
     * @param n The number of elements to pick
     * @param <E> The type of the elements to pick
     * @return An Iterable over the picked elements
     */
    public static <E> Iterable<E> pickAndRemove(final Iterable<? extends E> elements, final ElementSelectionStrategy<? super E> strategy, final int n) {
        final int size = Iterables.size(elements);
        checkArgument(size >= n, "Given elements are less that desired picks: {} < {}", size, n);
        return new Iterable<E>() {
            @Override
            public Iterator<E> iterator() {
                return new AbstractIterator<E>() {
                    
                    private final List<E> removed = Lists.newArrayList();
                    
                    @Override
                    protected E computeNext() {
                        if (removed.size() == n)
                            return endOfData();
                        else {
                            final E pick = strategy.pick(Iterables.filter(elements, Predicates.in(removed)));
                            removed.add(pick);
                            return pick;
                        }
                    }
                };
            }
        };
    }
}
