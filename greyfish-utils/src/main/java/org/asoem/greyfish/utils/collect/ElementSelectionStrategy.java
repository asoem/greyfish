package org.asoem.greyfish.utils.collect;

import java.util.List;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 12:50
 */
public interface ElementSelectionStrategy<E> {
    <T extends E> Iterable<T> pick(List<? extends T> elements, int k);
}
