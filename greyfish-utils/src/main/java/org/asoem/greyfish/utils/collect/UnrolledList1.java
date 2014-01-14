package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

final class UnrolledList1<E> extends ImmutableFunctionalList<E> implements Serializable {
    private final E element;

    UnrolledList1(final E element) {
        this.element = checkNotNull(element);
    }

    @Override
    public E get(final int index) {
        checkElementIndex(index, size());
        return element;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Iterable<E> filter(final Predicate<? super E> predicate) {
        return checkNotNull(predicate).apply(element) ? this : ImmutableList.<E>of();
    }

    @Override
    public boolean any(final Predicate<E> predicate) {
        return checkNotNull(predicate).apply(element);
    }

    @Override
    public Optional<E> findFirst(final Predicate<? super E> predicate) {
        return checkNotNull(predicate).apply(element) ? Optional.of(element) : Optional.<E>absent();
    }

    private void readObject(final ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (element == null) {
            throw new InvalidObjectException("Class does not accept null values");
        }
    }
}
