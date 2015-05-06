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

package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public class LinearSequences {
    private LinearSequences() {
    }

    /**
     * Create a crossover product between two {@code Iterables} with crossovers at given {@code indices}. This means
     * that both {@code Iterable}s in the returned product are a combination of the input iterables in such a way, that
     * the constructed iterable switches the input iterable at each given position. Both input {@code Iterable}s will be
     * zipped with {@link Products#zip(Iterable, Iterable)}. Therefore the returned {@code Iterable}s will have the same
     * size equal to the size of the input iterable with the fewest elements.
     *
     * @param x       The first Iterable
     * @param y       The second Iterable
     * @param indices the indices at which to do the crossovers
     * @param <E>     the type of the elements in {@code Iterable}s
     * @return a product of Iterables with crossovers at the given indices
     */
    public static <E> Product2<Iterable<E>, Iterable<E>> crossover(final Iterable<E> x, final Iterable<E> y, final Set<Integer> indices) {
        checkNotNull(x);
        checkNotNull(y);
        checkNotNull(indices);

        final Iterable<Product2<E, E>> zipped = Products.zip(x, y);

        if (indices.isEmpty()) {
            return Products.unzip(zipped);
        } else {
            final FunctionalList<Range<Integer>> ranges = ImmutableFunctionalList.copyOf(Iterables.transform(Iterables.partition(
                    Ordering.natural().immutableSortedCopy(indices), 2), new Function<List<Integer>, Range<Integer>>() {
                @Nullable
                @Override
                public Range<Integer> apply(@Nullable final List<Integer> input) {
                    assert input != null;
                    return input.size() == 2 ? Range.closed(input.get(0), input.get(1)) : Range.atLeast(input.get(0));
                }
            }));

            return Products.unzip(Iterables.transform(Products.zipWithIndex(zipped), new Function<Product2<Product2<E, E>, Integer>, Product2<E, E>>() {
                @Nullable
                @Override
                public Product2<E, E> apply(@Nullable final Product2<Product2<E, E>, Integer> input) {
                    assert input != null;
                    return ranges.any(new Predicate<Range<Integer>>() {
                        @Override
                        public boolean apply(@Nullable final Range<Integer> range) {
                            assert range != null;
                            return range.contains(input.second());
                        }
                    }) ? Products.swap(input.first()) : input.first();
                }
            }));
        }
    }

    /**
     * Computes the <a href="https://en.wikipedia.org/wiki/Hamming_distance">hamming distance</a> between the two lists
     * {@code first} and {@code second} which must be of equal length.
     *
     * @param first  the first list
     * @param second the second list
     * @return the number of positions where the elements in both lists are not {@link Object#equals equal}
     * @throws java.lang.IllegalArgumentException is the lists are not of the same length
     */
    public static <T> int hammingDistance(final List<T> first, final List<T> second) {
        checkArgument(checkNotNull(first).size() == checkNotNull(second.size()));
        final Iterable<Product2<T, T>> zipped = Products.zip(first, second);
        int sum = 0;
        for (final Product2<T, T> el : zipped) {
            if (!Objects.equal(el.first(), el.second())) {
                ++sum;
            }
        }
        return sum;
    }
}
