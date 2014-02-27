package org.asoem.greyfish.utils.collect;

import java.util.Collection;


public interface Sampling<E> {
    <T extends E> Iterable<T> sample(Collection<? extends T> elements, int k);
}
