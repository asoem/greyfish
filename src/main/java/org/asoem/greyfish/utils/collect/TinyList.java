package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: christoph
 * Date: 23.09.12
 * Time: 12:07
 */
public interface TinyList<E> extends Collection<E>,List<E> {
    E find(Predicate<? super E> predicate) throws NoSuchElementException;
    E find(Predicate<? super E> predicate, E defaultValue);
}
