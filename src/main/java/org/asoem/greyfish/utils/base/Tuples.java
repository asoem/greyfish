package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 15:57
 */
public class Tuples {
    public static <E1, E2> Tuple2<E1, E2> of(E1 e1, E2 e2) {
        return new Tuple2Impl<E1, E2>(e1, e2);
    }
}
