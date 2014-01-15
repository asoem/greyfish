package org.asoem.greyfish.utils.collect;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Tuple2 is an immutable implementation of {@code Product2} which does not allow {@code null} values.
 */
public class Tuple2<E1, E2> implements Product2<E1, E2> {

    private final E1 e1;
    private final E2 e2;

    private Tuple2(final E1 e1, final E2 e2) {
        assert e1 != null;
        assert e2 != null;
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public E1 first() {
        return e1;
    }

    @Override
    public E2 second() {
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Tuple2 tuple2 = (Tuple2) o;

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

    public static <E1, E2> Tuple2<E1, E2> of(final E1 e1, final E2 e2) {
        checkNotNull(e1);
        checkNotNull(e2);
        return new Tuple2<>(e1, e2);
    }

    @SuppressWarnings("unchecked") // safe cast
    public static <E1, E2> Tuple2<E1, E2> copyOf(final Product2<? extends E1, ? extends E2> product2) {
        if (product2 instanceof Tuple2) {
            return (Tuple2<E1, E2>) product2;
        } else {
            return of(product2.first(), product2.second());
        }
    }

    public static <E1, E2> Tuple2<E2, E1> swapped(final Product2<? extends E1, ? extends E2> product2) {
        return of(product2.second(), product2.first());
    }
}
