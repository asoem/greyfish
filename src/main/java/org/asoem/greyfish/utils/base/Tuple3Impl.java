package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 13:21
 */
public class Tuple3Impl<E1, E2, E3> implements Tuple3<E1, E2, E3> {

    private final E1 e1;
    private final E2 e2;
    private final E3 e3;

    public Tuple3Impl(E1 e1, E2 e2, E3 e3) {
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
}
