package org.asoem.greyfish.utils.collect;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 12:50
 */
public interface ElementSelectionStrategy<E> {
    <T extends E> T pick(Iterable<? extends T> elements);
}
