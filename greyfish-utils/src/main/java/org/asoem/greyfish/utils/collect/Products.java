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
import com.google.common.collect.*;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility functions that deal with generic products of values.
 */
public final class Products {
    private Products() {
    }

    /**
     * Create a view of {@code product} with swapped elements.
     *
     * @param product the product to swap
     * @param <E1>    the type of the first product element
     * @param <E2>    the type of the second product element
     * @return a view of the product with swapped elements
     */
    public static <E1, E2> Product2<E1, E2> swap(final Product2<E2, E1> product) {
        return Swapped.of(product);
    }

    /**
     * Create a view of the iterable of products as a product of iterables of the {@link Product2#first() first} and
     * {@link org.asoem.greyfish.utils.collect.Product2#second() second} elements respectively.
     *
     * @param products the iterable to unzip
     * @param <E1>     the type of the first product element
     * @param <E2>     the type of the second product element
     * @return a view of the iterable of products as a product of iterables
     */
    public static <E1, E2> Product2<Iterable<E1>, Iterable<E2>> unzip(
            final Iterable<? extends Product2<? extends E1, ? extends E2>> products) {
        final Iterable<E1> transform = Iterables.transform(products, new Function<Product2<? extends E1, ? extends E2>, E1>() {

            @Override
            public E1 apply(final Product2<? extends E1, ? extends E2> product2) {
                return checkNotNull(product2).first();
            }
        });
        final Iterable<E2> transform1 = Iterables.transform(products, new Function<Product2<? extends E1, ? extends E2>, E2>() {

            @Override
            public E2 apply(final Product2<? extends E1, ? extends E2> product2) {
                return checkNotNull(product2).second();
            }
        });
        return Tuple2.of(transform, transform1);
    }

    /**
     * Create a view of the product of iterables as an iterable of products of their elements in order. The size of the
     * returned iterable is equal to the size of the shortest iterable.
     *
     * @param toZip the product of iterables to zip
     * @param <E1>  the type of the elements in the first iterable
     * @param <E2>  the type of the elements in the second iterable
     * @return an iterable of products
     */
    public static <E1, E2> Iterable<Product2<E1, E2>> zip(
            final Product2<? extends Iterable<? extends E1>, ? extends Iterable<? extends E2>> toZip) {
        checkNotNull(toZip);
        return new Zip2<>(toZip.first(), toZip.second());
    }


    /**
     * Create a view of the two iterables {@code i1} and {@code i2} as an iterable of products of their elements in
     * order. The size of the returned iterable is equal to the size of the shortest iterable.
     *
     * @param i1   the first iterable
     * @param i2   the second iterable
     * @param <E1> the type of the elements in the first iterable
     * @param <E2> the type of the elements in the second iterable
     * @return an iterable of products
     */
    public static <E1, E2> Iterable<Product2<E1, E2>> zip(
            final Iterable<? extends E1> i1, final Iterable<? extends E2> i2) {
        checkNotNull(i1);
        checkNotNull(i2);
        return new Zip2<>(i1, i2);
    }

    /**
     * Create a view of the product of iterables as an iterable of products of their elements in order. The size of the
     * returned iterable is equal to the size of the shortest iterable.
     *
     * @param toZip the product of iterables to zip
     * @param <E1>  the type of the elements in the first iterable
     * @param <E2>  the type of the elements in the second iterable
     * @return an iterable of products
     */
    public static <E1, E2, E3> Iterable<Product3<E1, E2, E3>> zip(
            final Product3<? extends Iterable<? extends E1>, ? extends Iterable<? extends E2>,
                    ? extends Iterable<? extends E3>> toZip) {
        checkNotNull(toZip);
        return new Zip3<>(toZip.first(), toZip.second(), toZip.third());
    }

    public static <E1, E2, E3> Iterable<Product3<E1, E2, E3>> zip(
            final Iterable<? extends E1> i1, final Iterable<? extends E2> i2, final Iterable<? extends E3> i3) {
        checkNotNull(i1);
        checkNotNull(i2);
        checkNotNull(i3);
        return new Zip3<>(i1, i2, i3);
    }

    /**
     * Zips the {@code iterable} with its indices of the iteration.
     *
     * @param iterable the elements to zip with their indices
     * @param <E>      the type of the elements of the iterable
     * @return an iterable of products
     */
    public static <E> Iterable<Product2<E, Integer>> zipWithIndex(final Iterable<? extends E> iterable) {
        checkNotNull(iterable);
        return new Zip2<>(iterable, ContiguousSet.create(Range.atLeast(0), DiscreteDomain.integers()));
    }

    /**
     * Create a view of the two iterables {@code i1} and {@code i2} as an iterable of products of their elements in
     * order. If the sizes of the iterables differ the shorter is padded with {@code thisElement} or {@code thatElement}
     * respectively.
     *
     * @param i1          the first iterable
     * @param i2          the second iterable
     * @param thisElement the padding element for the first iterable
     * @param thatElement the padding element for the second iterable
     * @param <E1>        the type of the elements in the first iterable
     * @param <E2>        the type of the elements in the second iterable
     * @return an iterable of products
     */
    public static <E1, E2> Iterable<Product2<E1, E2>> zipAll(
            final Iterable<? extends E1> i1, final Iterable<? extends E2> i2,
            final E1 thisElement, final E2 thatElement) {
        return new ZipAll2<>(i1, i2, thisElement, thatElement);
    }

    private static class Swapped<E1, E2> implements Product2<E1, E2> {
        private final Product2<E2, E1> product;

        private Swapped(final Product2<E2, E1> product) {
            this.product = product;
        }

        public static <E2, E1> Product2<E2, E1> of(final Product2<E1, E2> product) {
            checkNotNull(product);
            if (product instanceof Swapped) {
                return ((Swapped<E1, E2>) product).product;
            } else {
                return new Swapped<>(product);
            }
        }

        @Override
        public E1 first() {
            return product.second();
        }

        @Override
        public E2 second() {
            return product.first();
        }
    }

    private static class Zip2<E1, E2> implements Iterable<Product2<E1, E2>> {

        private final Iterable<? extends E1> i1;
        private final Iterable<? extends E2> i2;

        public Zip2(final Iterable<? extends E1> i1, final Iterable<? extends E2> i2) {
            this.i1 = i1;
            this.i2 = i2;
        }

        @Override
        public Iterator<Product2<E1, E2>> iterator() {

            return new AbstractIterator<Product2<E1, E2>>() {
                private final Iterator<? extends E1> iterator1 = i1.iterator();
                private final Iterator<? extends E2> iterator2 = i2.iterator();

                @Override
                protected Product2<E1, E2> computeNext() {
                    if (iterator1.hasNext() && iterator2.hasNext()) {
                        return Tuple2.of(iterator1.next(), iterator2.next());
                    } else {
                        return endOfData();
                    }
                }
            };
        }

    }

    private static class ZipAll2<E1, E2> implements Iterable<Product2<E1, E2>> {
        private final Iterable<? extends E1> i1;
        private final Iterable<? extends E2> i2;
        private final E1 thisElement;
        private final E2 thatElement;

        public ZipAll2(final Iterable<? extends E1> i1, final Iterable<? extends E2> i2,
                       final E1 thisElement, final E2 thatElement) {
            this.i1 = i1;
            this.i2 = i2;
            this.thisElement = thisElement;
            this.thatElement = thatElement;
        }

        @Override
        public Iterator<Product2<E1, E2>> iterator() {
            return new AbstractIterator<Product2<E1, E2>>() {
                private Iterator<? extends E1> it1 = i1.iterator();
                private Iterator<? extends E2> it2 = i2.iterator();

                @Override
                protected Product2<E1, E2> computeNext() {
                    if (it1.hasNext() && it2.hasNext()) {
                        return Tuple2.of(it1.next(), it2.next());
                    } else if (it1.hasNext() && !it2.hasNext()) {
                        return Tuple2.of(it1.next(), thatElement);
                    } else if (!it1.hasNext() && it2.hasNext()) {
                        return Tuple2.of(thisElement, it2.next());
                    } else {
                        return endOfData();
                    }
                }
            };
        }
    }

    private static class Zip3<E1, E2, E3> implements Iterable<Product3<E1, E2, E3>> {
        private final Iterable<? extends E1> first;
        private final Iterable<? extends E2> second;
        private final Iterable<? extends E3> third;

        public Zip3(final Iterable<? extends E1> first, final Iterable<? extends E2> second, final Iterable<? extends E3> third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        @Override
        public Iterator<Product3<E1, E2, E3>> iterator() {
            return new AbstractIterator<Product3<E1, E2, E3>>() {
                private final Iterator<? extends E1> iterator1 = first.iterator();
                private final Iterator<? extends E2> iterator2 = second.iterator();
                private final Iterator<? extends E3> iterator3 = third.iterator();

                @Override
                protected Product3<E1, E2, E3> computeNext() {
                    if (iterator1.hasNext() && iterator2.hasNext() && iterator3.hasNext()) {
                        return Tuple3.of(iterator1.next(), iterator2.next(), iterator3.next());
                    } else {
                        return endOfData();
                    }
                }
            };
        }
    }
}
