package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 15:55
 */
public class Tuple2<E1, E2> implements Product2<E1, E2> {

    private final E1 e1;
    private final E2 e2;

    private Tuple2(E1 e1, E2 e2) {
        assert e1 != null;
        assert e2 != null;
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public E1 _1() {
        return e1;
    }

    @Override
    public E2 _2() {
        return e2;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(Tuple2.class)
                .addValue(e1)
                .addValue(e2)
                .toString();
    }

    @SuppressWarnings({"rawtypes", "RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple2 tuple2 = (Tuple2) o;

        if (!e1.equals(tuple2.e1)) return false;
        if (!e2.equals(tuple2.e2)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = e1.hashCode();
        result = 31 * result + e2.hashCode();
        return result;
    }

    public static <E1, E2> Tuple2<E1, E2> of(E1 e1, E2 e2) {
        checkNotNull(e1);
        checkNotNull(e2);
        return new Tuple2<E1, E2>(e1, e2);
    }

    public static <I1 extends Iterable<E1>, I2 extends Iterable<E2>, E1, E2> Zipped<E1, I1, E2, I2> zipped(Product2<I1, I2> tupleOfIterables) {
        checkNotNull(tupleOfIterables);
        return Zipped.of(tupleOfIterables);
    }

    public static <I1 extends Iterable<E1>, I2 extends Iterable<E2>, E1, E2> Zipped<E1, I1, E2, I2> zipped(I1 iterable1, I2 iterable2) {
        return Zipped.of(iterable1, iterable2);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E1, E2> Tuple2<Iterable<E1>, Iterable<E2>> unzipped(final Iterable<? extends Product2<E1, E2>> zipped) {
        if (zipped instanceof Zipped) {
            final Zipped<E1, Iterable<E1>, E2, Iterable<E2>> zipped1 = (Zipped<E1, Iterable<E1>, E2, Iterable<E2>>) (Zipped) zipped;
            return of(zipped1._1(), zipped1._2());
        }
        else {
            final Iterable<E1> transform = Iterables.transform(zipped, new Function<Product2<E1, E2>, E1>() {

                @Override
                public E1 apply(Product2<E1, E2> e1E2Product2) {
                    return checkNotNull(e1E2Product2)._1();
                }
            });
            final Iterable<E2> transform1 = Iterables.transform(zipped, new Function<Product2<E1, E2>, E2>() {

                @Override
                public E2 apply(Product2<E1, E2> e1E2Product2) {
                    return checkNotNull(e1E2Product2)._2();
                }
            });
            return Tuple2.of(transform, transform1);
        }
    }

    public static <E1, E2> Tuple2<E1, E2> copyOf(Product2<E1, E2> product2) {
        if (product2 instanceof Tuple2)
            return (Tuple2<E1, E2>) product2;
        else
            return of(product2._1(), product2._2());
    }

    public static <E1, E2> Tuple2<E2, E1> swap(Product2<E1, E2> input) {
        return Tuple2.of(input._2(), input._1());
    }

    public static class Zipped<E1, I1 extends Iterable<E1>, E2, I2 extends Iterable<E2>> extends Tuple2<I1, I2> implements Iterable<Product2<E1, E2>> {

        private Zipped(I1 e1s, I2 e2s) {
            super(e1s, e2s);
        }

        @Override
        public Iterator<Product2<E1, E2>> iterator() {

            return new AbstractIterator<Product2<E1, E2>>() {
                private final Iterator<? extends E1> iterator1 = _1().iterator();
                private final Iterator<? extends E2> iterator2 = _2().iterator();

                @Override
                protected Product2<E1, E2> computeNext() {
                    if (iterator1.hasNext() && iterator2.hasNext())
                        return Tuple2.of(iterator1.next(), iterator2.next());
                    else
                        return endOfData();
                }
            };
        }

        private static <E1, I1 extends Iterable<E1>, E2, I2 extends Iterable<E2>> Zipped<E1, I1, E2, I2> of(Product2<I1, I2> delegate) {
            checkArgument(Iterables.size(delegate._1()) == Iterables.size(delegate._2()));
            return new Zipped<E1, I1, E2, I2>(delegate._1(), delegate._2());
        }

        private static <E1, I1 extends Iterable<E1>, E2, I2 extends Iterable<E2>> Zipped<E1, I1, E2, I2> of(I1 iterable1, I2 iterable2) {
            checkArgument(Iterables.size(iterable1) == Iterables.size(iterable2));
            return new Zipped<E1, I1, E2, I2>(iterable1, iterable2);
        }
    }
}
