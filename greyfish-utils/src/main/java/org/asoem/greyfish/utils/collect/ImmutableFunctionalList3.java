package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;

/**
 * User: christoph
 * Date: 21.09.12
 * Time: 15:37
 */
class ImmutableFunctionalList3<E> extends ImmutableFunctionalList<E> implements Serializable, FunctionalList<E> {

    final private E e0;
    final private E e1;
    final private E e2;

    ImmutableFunctionalList3(final E e0, final E e1, final E e2) {
        this.e0 = checkNotNull(e0);
        this.e1 = checkNotNull(e1);
        this.e2 = checkNotNull(e2);
    }

    @Override
    public E get(final int index) {
        switch (index) {
            case 0: return e0;
            case 1: return e1;
            case 2: return e2;
            default: checkPositionIndex(index, size()); throw new AssertionError("unreachable");
        }
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public Optional<E> findFirst(final Predicate<? super E> predicate) {
        checkNotNull(predicate, "Predicate is null");
        if (predicate.apply(e0)) {
            return Optional.of(e0);
        } else if (predicate.apply(e1)) {
            return Optional.of(e1);
        } else if (predicate.apply(e2)) {
            return Optional.of(e2);
        } else {
            return Optional.absent();
        }
    }

    private void readObject(final ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (e0 == null || e1 == null || e2 == null)
            throw new InvalidObjectException("Class does not accept null values");
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2) {
        return new ImmutableFunctionalList3<E>(e0, e1, e2);
    }
}
