package org.asoem.greyfish.utils.collect;

import com.google.common.base.Objects;

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

    public static <E1, E2> Tuple2<E1, E2> copyOf(Product2<E1, E2> product2) {
        if (product2 instanceof Tuple2)
            return (Tuple2<E1, E2>) product2;
        else
            return of(product2._1(), product2._2());
    }

}
