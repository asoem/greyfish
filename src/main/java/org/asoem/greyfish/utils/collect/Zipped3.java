package org.asoem.greyfish.utils.collect;

import org.asoem.greyfish.utils.base.Tuple3;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 13:10
 */
public interface Zipped3<E1, S1 extends Iterable<E1>, E2, S2 extends Iterable<E2>, E3, S3 extends Iterable<E3>>
        extends Iterable<Tuple3<E1, E2, E3>>, Tuple3<S1, S2, S3> {
}
