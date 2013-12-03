package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.AbstractList;
import java.util.List;

/**
 * Abstract base class for 'functional' lists.
 */
abstract class AbstractFunctionalList<E> extends AbstractList<E> implements FunctionalList<E> {

    @Override
    public Iterable<E> filter(final Predicate<? super E> predicate) {
        return Iterables.filter(this, predicate);
    }

    @Override
    public boolean any(final Predicate<E> predicate) {
        return Iterables.any(this, predicate);
    }

    @Override
    public Optional<E> findFirst(final Predicate<? super E> predicate) {
        return Iterables.tryFind(this, predicate);
    }

    @Override
    public List<E> remove(final Predicate<? super E> predicate) {
        final ImmutableList<E> remove = ImmutableList.copyOf(filter(predicate));
        Iterables.removeAll(this, remove);
        return remove;
    }
}
