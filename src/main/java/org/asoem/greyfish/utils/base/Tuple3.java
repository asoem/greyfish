package org.asoem.greyfish.utils.base;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;

import java.util.Iterator;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 13:21
 */
public class Tuple3<E1, E2, E3> implements Product3<E1, E2, E3> {

    private final E1 e1;
    private final E2 e2;
    private final E3 e3;

    public Tuple3(E1 e1, E2 e2, E3 e3) {
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
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
    public E3 _3() {
        return e3;
    }

    public static <E1, E2, E3> Tuple3<E1, E2, E3> of(E1 e1, E2 e2, E3 e3) {
        return new Tuple3<E1, E2, E3>(e1, e2, e3);
    }

    public <I1, I2, I3> Zipped<I1, I2, I3> zipped(
            Function<? super E1, ? extends Iterable<I1>> f1,
            Function<? super E2, ? extends Iterable<I2>> f2,
            Function<? super E3, ? extends Iterable<I3>> f3) {
        return new Zipped<I1, I2, I3>(f1.apply(e1), f2.apply(e2), f3.apply(e3));
    }

    /**
     * This is the convenient version of {@link #zipped(com.google.common.base.Function, com.google.common.base.Function, com.google.common.base.Function)}
     * which uses an implicit conversion (type cast) of the elements in this tuple.
     * @param <I1> The element type of the assumed {@code Iterable} {@link #_1()}
     * @param <I2> The element type of the assumed {@code Iterable} {@link #_2()}
     * @param <I3> The element type of the assumed {@code Iterable} {@link #_3()}
     * @return A zipped version of this tuple if all elements can be cast into Iterables of the given types
     * @throws UnsupportedOperationException if the elements in this tuple cannot be converted into Iterables of I1, I2 and I3 respectively
     */
    @SuppressWarnings("unchecked")
    public <I1, I2, I3> Zipped<I1, I2, I3> zipped() throws UnsupportedOperationException {
        try {
            final Iterable<I1> i1 = (Iterable<I1>) e1;
            final Iterable<I2> i2 = (Iterable<I2>) e2;
            final Iterable<I3> i3 = (Iterable<I3>) e3;

            return new Zipped<I1, I2, I3>(i1, i2, i3);
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("One of this tuple's elements cannot be cast to the desired iterable", e);
        }
    }

    public static class Zipped<I1, I2, I3> extends Tuple3<Iterable<I1>, Iterable<I2>, Iterable<I3>> implements Iterable<Product3<I1, I2, I3>> {

        public Zipped(Iterable<I1> i1, Iterable<I2> i2, Iterable<I3> i3) {
            super(i1, i2, i3);
        }

        @Override
        public Iterator<Product3<I1, I2, I3>> iterator() {

            return new AbstractIterator<Product3<I1, I2, I3>>() {
                private final Iterator<? extends I1> iterator1 = _1().iterator();
                private final Iterator<? extends I2> iterator2 = _2().iterator();
                private final Iterator<? extends I3> iterator3 = _3().iterator();

                @Override
                protected Product3<I1, I2, I3> computeNext() {
                    if (iterator1.hasNext() && iterator2.hasNext() && iterator3.hasNext())
                        return new Tuple3<I1, I2, I3>(iterator1.next(), iterator2.next(), iterator3.next());
                    else
                        return endOfData();

                }
            };
        }

        public static <I1, I2, I3> Zipped<I1, I2, I3> of(Iterable<I1> i1, Iterable<I2> i2, Iterable<I3> i3) {
            return new Zipped<I1, I2, I3>(i1, i2, i3);
        }
    }
}
