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
public abstract class AbstractFunctionalList<E> extends AbstractList<E> implements FunctionalList<E> {
    @Override
    public E find(final Predicate<? super E> predicate) throws NoSuchElementException {
        return Iterables.find(this, predicate);
    }

    @Override
    public E find(final Predicate<? super E> predicate, final E defaultValue) {
        return Iterables.find(this, predicate, defaultValue);
    }

    @Override
    public Iterable<E> filter(final Predicate<? super E> predicate) {
        return Iterables.filter(this, predicate);
    }

    @Override
    public boolean any(final Predicate<E> predicate) {
        return Iterables.any(this, predicate);
    }
}
