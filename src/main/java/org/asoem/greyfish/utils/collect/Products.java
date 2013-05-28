package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.*;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 08.02.13
 * Time: 10:35
 */
public class Products {
    private Products() {}

    public static <E1, E2> Product2<E2, E1> swap(final Product2<E1, E2> product) {
        return SwappedProduct2.of(product);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E1, E2> Product2<Iterable<E1>, Iterable<E2>> unzip(Iterable<? extends Product2<E1, E2>> products) {
        final Iterable<E1> transform = Iterables.transform(products, new Function<Product2<E1, E2>, E1>() {

            @Override
            public E1 apply(Product2<E1, E2> e1E2Product2) {
                return checkNotNull(e1E2Product2)._1();
            }
        });
        final Iterable<E2> transform1 = Iterables.transform(products, new Function<Product2<E1, E2>, E2>() {

            @Override
            public E2 apply(Product2<E1, E2> e1E2Product2) {
                return checkNotNull(e1E2Product2)._2();
            }
        });
        return Tuple2.of(transform, transform1);
    }

    public static <E1, E2> Iterable<Product2<E1, E2>> zip(Iterable<E1> i1, Iterable<E2> i2) {
        checkNotNull(i1);
        checkNotNull(i2);
        return new Zip2<E1, E2>(i1, i2);
    }

    public static <E1, E2, E3> Iterable<Product3<E1, E2, E3>> zip(Iterable<E1> i1, Iterable<E2> i2, Iterable<E3> i3) {
        checkNotNull(i1);
        checkNotNull(i2);
        checkNotNull(i3);
        return new Zip3<E1, E2, E3>(i1, i2, i3);
    }

    public static <E> Iterable<Product2<E, Integer>> zipWithIndex(Iterable<E> i1) {
        checkNotNull(i1);
        return new Zip2<E, Integer>(i1, ContiguousSet.create(Range.atLeast(0), DiscreteDomain.integers()));
    }

    public static <E1, E2> Iterable<Product2<E1, E2>> zipAll(Iterable<E1> i1, Iterable<E2> i2, E1 thisElement, E2 thatElement) {
        return new ZipAll2<E1, E2>(i1, i2, thisElement, thatElement);
    }

    private static class SwappedProduct2<E2, E1> implements Product2<E2, E1> {
        private final Product2<E1, E2> product;

        private SwappedProduct2(Product2<E1, E2> product) {
            this.product = product;
        }

        public static <E2, E1> Product2<E2, E1> of(Product2<E1, E2> product) {
            checkNotNull(product);
            if (product instanceof SwappedProduct2)
                return ((SwappedProduct2<E1, E2>) product).product;
            else
                return new SwappedProduct2<E2, E1>(product);
        }

        @Override
        public E2 _1() {
            return product._2();
        }

        @Override
        public E1 _2() {
            return product._1();
        }
    }

    private static class Zip2<E1, E2> implements Iterable<Product2<E1, E2>> {

        private final Iterable<E1> i1;
        private final Iterable<E2> i2;

        public Zip2(Iterable<E1> i1, Iterable<E2> i2) {
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
                    if (iterator1.hasNext() && iterator2.hasNext())
                        return Tuple2.of(iterator1.next(), iterator2.next());
                    else
                        return endOfData();
                }
            };
        }

    }

    private static class ZipAll2<E1, E2> implements Iterable<Product2<E1, E2>> {
        private final Iterable<E1> i1;
        private final Iterable<E2> i2;
        private final E1 thisElement;
        private final E2 thatElement;

        public ZipAll2(Iterable<E1> i1, Iterable<E2> i2, E1 thisElement, E2 thatElement) {
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
                    if (it1.hasNext() && it2.hasNext())
                        return Tuple2.of(it1.next(), it2.next());
                    else if (it1.hasNext() && !it2.hasNext())
                        return Tuple2.of(it1.next(), thatElement);
                    else if (!it1.hasNext() && it2.hasNext())
                        return Tuple2.of(thisElement, it2.next());
                    else
                        return endOfData();
                }
            };
        }
    }

    /**
    * User: christoph
    * Date: 28.05.13
    * Time: 15:16
    */
    private static class Zip3<E1, E2, E3> extends Tuple3<Iterable<E1>, Iterable<E2>, Iterable<E3>> implements Iterable<Product3<E1, E2, E3>> {

        public Zip3(Iterable<E1> i1, Iterable<E2> i2, Iterable<E3> i3) {
            super(i1, i2, i3);
        }

        @Override
        public Iterator<Product3<E1, E2, E3>> iterator() {

            return new AbstractIterator<Product3<E1, E2, E3>>() {
                private final Iterator<? extends E1> iterator1 = _1().iterator();
                private final Iterator<? extends E2> iterator2 = _2().iterator();
                private final Iterator<? extends E3> iterator3 = _3().iterator();

                @Override
                protected Product3<E1, E2, E3> computeNext() {
                    if (iterator1.hasNext() && iterator2.hasNext() && iterator3.hasNext())
                        return new Tuple3<E1, E2, E3>(iterator1.next(), iterator2.next(), iterator3.next());
                    else
                        return endOfData();

                }
            };
        }

        public static <I1, I2, I3> Zip3<I1, I2, I3> of(Iterable<I1> i1, Iterable<I2> i2, Iterable<I3> i3) {
            return new Zip3<I1, I2, I3>(i1, i2, i3);
        }
    }
}
