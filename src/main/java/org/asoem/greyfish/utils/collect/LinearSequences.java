package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 31.01.13
 * Time: 11:37
 */
public class LinearSequences {
    private LinearSequences() {}

    /**
     * Create a crossover product between two {@code Iterables} with crossovers at given {@code indices}.
     * This means that both {@code Iterable}s in the returned product are a combination of the input iterables in such a way,
     * that the constructed iterable switches the input iterable at each given position.
     * Both input {@code Iterable}s will be zipped with {@link Products#zip(Iterable, Iterable)}.
     * Therefore the returned {@code Iterable}s will have the same size equal to the size of the input iterable with the fewest elements.
     * @param x The first Iterable
     * @param y The second Iterable
     * @param indices the indices at which to do the crossovers
     * @param <E> the type of the elements in {@code Iterable}s
     * @return a product of Iterables with crossovers at the given indices
     */
    public static <E> Product2<Iterable<E>, Iterable<E>> crossover(Iterable<E> x, Iterable<E> y, final Set<Integer> indices) {
        checkNotNull(x);
        checkNotNull(y);
        checkNotNull(indices);

        final Iterable<Product2<E, E>> zipped = Products.zip(x, y);

        if (indices.isEmpty())
            return Products.unzip(zipped);
        else {
            final FunctionalList<Range<Integer>> ranges = ImmutableFunctionalList.copyOf(Iterables.transform(Iterables.partition(
                    Ordering.natural().immutableSortedCopy(indices), 2), new Function<List<Integer>, Range<Integer>>() {
                @Nullable
                @Override
                public Range<Integer> apply(@Nullable List<Integer> input) {
                    assert input != null;
                    return input.size() == 2 ? Ranges.closed(input.get(0), input.get(1)) : Ranges.atLeast(input.get(0));
                }
            }));

            return Products.unzip(Iterables.transform(Products.zipWithIndex(zipped), new Function<Product2<Product2<E, E>, Integer>, Product2<E, E>>() {
                @Nullable
                @Override
                public Product2<E, E> apply(@Nullable final Product2<Product2<E, E>, Integer> input) {
                    assert input != null;
                    return ranges.any(new Predicate<Range<Integer>>() {
                        @Override
                        public boolean apply(@Nullable Range<Integer> range) {
                            assert range != null;
                            return range.contains(input._2());
                        }
                    }) ? Products.swap(input._1()) : input._1();
                }
            }));
        }
    }

    public static <E> int hammingDistance(List<E> a, List<E> b) {
        checkArgument(checkNotNull(a).size() == checkNotNull(b.size()));
        final Iterable<Product2<E, E>> zipped = Products.zip(a, b);
        int sum = 0;
        for (Product2<E, E> el : zipped) {
            if (!el._1().equals(el._2()))
                ++sum;
        }
        return sum;
    }

}
