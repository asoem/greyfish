package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;

final class ImmutableFunctionalList5<E> extends ImmutableFunctionalList<E> implements Serializable, FunctionalList<E> {

    private final E e0;
    private final E e1;
    private final E e2;
    private final E e3;
    private final E e4;

    ImmutableFunctionalList5(final E e0, final E e1, final E e2, final E e3, final E e4) {
        this.e0 = checkNotNull(e0);
        this.e1 = checkNotNull(e1);
        this.e2 = checkNotNull(e2);
        this.e3 = checkNotNull(e3);
        this.e4 = checkNotNull(e4);
    }

    @Override
    public E get(final int index) {
        switch (index) {
            case 0:
                return e0;
            case 1:
                return e1;
            case 2:
                return e2;
            case 3:
                return e3;
            case 4:
                return e4;
            default:
                checkPositionIndex(index, size());
                throw new AssertionError("unreachable");
        }
    }

    @Override
    public int size() {
        return 5;
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
        } else if (predicate.apply(e3)) {
            return Optional.of(e3);
        } else if (predicate.apply(e4)) {
            return Optional.of(e4);
        } else {
            return Optional.absent();
        }
    }


    private void readObject(final ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (e0 == null || e1 == null || e2 == null || e3 == null || e4 == null) {
            throw new InvalidObjectException("Class does not accept null values");
        }
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2, final E e3, final E e4) {
        return new ImmutableFunctionalList5<>(e0, e1, e2, e3, e4);
    }
}
