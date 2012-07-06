package org.asoem.greyfish.utils.base;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 15:55
 */
public class Tuple2<E1, E2> implements Product2<E1, E2> {
    @Element(name = "e1")
    private final E1 e1;

    @Element(name = "e2")
    private final E2 e2;

    public Tuple2(@Element(name = "e1")E1 e1, @Element(name = "e2")E2 e2) {
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

    public static <E1, E2> Tuple2<E1, E2> of(E1 e1, E2 e2) {
        return new Tuple2<E1, E2>(e1, e2);
    }

    public static <E1, E2> Tuple2<Iterable<E1>, Iterable<E2>> unzipped(final Iterable<? extends Product2<E1, E2>> zipped) {
        final Iterable<E1> transform = Iterables.transform(zipped, new Function<Product2<E1, E2>, E1>() {

            @Override
            public E1 apply(@Nullable Product2<E1, E2> e1E2Product2) {
                return checkNotNull(e1E2Product2)._1();
            }
        });
        final Iterable<E2> transform1 = Iterables.transform(zipped, new Function<Product2<E1, E2>, E2>() {

            @Override
            public E2 apply(@Nullable Product2<E1, E2> e1E2Product2) {
                return checkNotNull(e1E2Product2)._2();
            }
        });
        return Tuple2.of(transform, transform1);
    }

    public static class Zipped<I1, I2> extends Tuple2<Iterable<I1>, Iterable<I2>> implements Iterable<Product2<I1, I2>> {

        public Zipped(Iterable<I1> i1, Iterable<I2> i2) {
            super(i1, i2);
        }

        @Override
        public Iterator<Product2<I1, I2>> iterator() {

            return new AbstractIterator<Product2<I1, I2>>() {
                private final Iterator<? extends I1> iterator1 = _1().iterator();
                private final Iterator<? extends I2> iterator2 = _2().iterator();

                @Override
                protected Product2<I1, I2> computeNext() {
                    if (iterator1.hasNext() && iterator2.hasNext() )
                        return new Tuple2<I1, I2>(iterator1.next(), iterator2.next());
                    else
                        return endOfData();

                }
            };
        }

        public static <I1, I2> Zipped<I1, I2> of(Iterable<I1> i1, Iterable<I2> i2) {
            return new Zipped<I1, I2>(i1, i2);
        }
    }
}
