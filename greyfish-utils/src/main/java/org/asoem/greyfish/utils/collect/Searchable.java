package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;

import java.util.NoSuchElementException;

/**
 * User: christoph
 * Date: 27.09.12
 * Time: 13:48
 */
public interface Searchable<E> {
    E find(Predicate<? super E> predicate) throws NoSuchElementException;
    E find(Predicate<? super E> predicate, E defaultValue);
    Iterable<E> filter(Predicate<? super E> predicate);
}
