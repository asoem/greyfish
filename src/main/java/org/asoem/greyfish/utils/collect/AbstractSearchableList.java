package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.AbstractList;
import java.util.NoSuchElementException;

/**
 * User: christoph
 * Date: 15.01.13
 * Time: 18:00
 */
public abstract class AbstractSearchableList<E> extends AbstractList<E> implements SearchableList<E> {
    @Override
    public E find(Predicate<? super E> predicate) throws NoSuchElementException {
        return Iterables.find(this, predicate);
    }

    @Override
    public E find(Predicate<? super E> predicate, E defaultValue) {
        return Iterables.find(this, predicate, defaultValue);
    }

    @Override
    public Iterable<E> filter(Predicate<? super E> predicate) {
        return Iterables.filter(this, predicate);
    }
}
