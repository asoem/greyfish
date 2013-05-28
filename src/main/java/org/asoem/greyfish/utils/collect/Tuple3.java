package org.asoem.greyfish.utils.collect;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple3)) return false;

        Tuple3 tuple3 = (Tuple3) o;

        if (!e1.equals(tuple3.e1)) return false;
        if (!e2.equals(tuple3.e2)) return false;
        if (!e3.equals(tuple3.e3)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = e1.hashCode();
        result = 31 * result + e2.hashCode();
        result = 31 * result + e3.hashCode();
        return result;
    }
}
