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

    public static <E1, E2> Product2<E2, E1> swap(final Product2<E1, E2> product) {
        return SwappedProduct2.of(product);
    }

    public static <E1, E2> Product2<Iterable<E1>, Iterable<E2>> unzip(
            final Iterable<? extends Product2<E1, E2>> products) {
        final Iterable<E1> transform = Iterables.transform(products, new Function<Product2<E1, E2>, E1>() {

            @Override
            public E1 apply(final Product2<E1, E2> product2) {
                return checkNotNull(product2).first();
            }
        });
        final Iterable<E2> transform1 = Iterables.transform(products, new Function<Product2<E1, E2>, E2>() {

            @Override
            public E2 apply(final Product2<E1, E2> product2) {
                return checkNotNull(product2).second();
            }
        });
        return Tuple2.of(transform, transform1);
    }

    public static <E1, E2> Iterable<Product2<E1, E2>> zip(final Iterable<E1> i1, final Iterable<E2> i2) {
        checkNotNull(i1);
        checkNotNull(i2);
        return new Zip2<>(i1, i2);
    }

    public static <E1, E2, E3> Iterable<Product3<E1, E2, E3>> zip(
            final Iterable<E1> i1, final Iterable<E2> i2, final Iterable<E3> i3) {
        checkNotNull(i1);
        checkNotNull(i2);
        checkNotNull(i3);
        return Tuple3.zipped(i1, i2, i3);
    }

    public static <E> Iterable<Product2<E, Integer>> zipWithIndex(final Iterable<E> i1) {
        checkNotNull(i1);
        return new Zip2<>(i1, ContiguousSet.create(Range.atLeast(0), DiscreteDomain.integers()));
    }

    public static <E1, E2> Iterable<Product2<E1, E2>> zipAll(
            final Iterable<E1> i1, final Iterable<E2> i2, final E1 thisElement, final E2 thatElement) {
        return new ZipAll2<>(i1, i2, thisElement, thatElement);
    }

    /**
     * A view of a product with swapped elements.
     *
     * @param <E1> the type of the first element
     * @param <E2> the type of the second element
     */
    private static class SwappedProduct2<E1, E2> implements Product2<E1, E2> {
        private final Product2<E2, E1> product;

        private SwappedProduct2(final Product2<E2, E1> product) {
            this.product = product;
        }

        public static <E2, E1> Product2<E2, E1> of(final Product2<E1, E2> product) {
            checkNotNull(product);
            if (product instanceof SwappedProduct2) {
                return ((SwappedProduct2<E1, E2>) product).product;
            } else {
                return new SwappedProduct2<>(product);
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

        private final Iterable<E1> i1;
        private final Iterable<E2> i2;

        public Zip2(final Iterable<E1> i1, final Iterable<E2> i2) {
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
        private final Iterable<E1> i1;
        private final Iterable<E2> i2;
        private final E1 thisElement;
        private final E2 thatElement;

        public ZipAll2(final Iterable<E1> i1, final Iterable<E2> i2, final E1 thisElement, final E2 thatElement) {
            this.i1 = i1;
            this.i2 = i2;
            this.thisElement = thisElement;
            this.thatElement = thatElement;
        }

        @Override
        public Iterator<Product2<E1, E2>> iterator() {
            return new AbstractIterator<Product2<E1, E2>>() {
                private Iterator<E1> it1 = i1.iterator();
                private Iterator<E2> it2 = i2.iterator();

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
}
