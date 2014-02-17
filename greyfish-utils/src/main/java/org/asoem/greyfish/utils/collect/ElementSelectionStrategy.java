package org.asoem.greyfish.utils.collect;

import java.util.List;


public interface ElementSelectionStrategy<E> {
    <T extends E> Iterable<T> pick(List<? extends T> elements, int k);
}
