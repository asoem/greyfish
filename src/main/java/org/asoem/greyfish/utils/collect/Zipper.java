package org.asoem.greyfish.utils.collect;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 12:57
 */
public class Zipper {
    public static <E1, S1 extends Iterable<E1>, E2, S2 extends Iterable<E2>, E3, S3 extends Iterable<E3>> Zipped3<E1, S1, E2, S2, E3, S3> zip(
            final S1 iterable1,
            final S2 iterable2,
            final S3 iterable3) {
        return new Zipped3Impl<E1, S1, E2, S2, E3, S3>(iterable1, iterable2, iterable3);
    }

}
