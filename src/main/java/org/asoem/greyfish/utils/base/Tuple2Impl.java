package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 15:55
 */
public class Tuple2Impl<E1, E2> implements Tuple2<E1, E2> {
    private final E1 e1;
    private final E2 e2;

    public Tuple2Impl(E1 e1, E2 e2) {
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
}
