package org.asoem.greyfish.utils.collect;


import org.asoem.greyfish.utils.base.Tuple2;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 11:29
 */
public interface Zipped2<E, S extends Iterable<E>> extends Iterable<Tuple2<E, E>>, Tuple2<S, S> {
}
