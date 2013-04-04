package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;

/**
 * User: christoph
 * Date: 21.09.12
 * Time: 15:37
 */
class ImmutableFunctionalList2<E> extends ImmutableFunctionalList<E> implements Serializable, FunctionalList<E> {

    final private E e0;
    final private E e1;

    ImmutableFunctionalList2(E e0, E e1) {
        this.e0 = checkNotNull(e0);
        this.e1 = checkNotNull(e1);
    }

    @Override
    public E get(int index) {
        switch (index) {
            case 0: return e0;
            case 1: return e1;
            default: checkPositionIndex(index, size()); throw new AssertionError("unreachable");
        }
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public E find(Predicate<? super E> predicate) {
        checkNotNull(predicate, "Predicate is null");
        if (predicate.apply(e0))
            return e0;
        if (predicate.apply(e1))
            return e1;
        throw new NoSuchElementException();
    }

    @Override
    public E find(Predicate<? super E> predicate, E defaultValue) {
        checkNotNull(predicate, "Predicate is null");
        if (predicate.apply(e0))
            return e0;
        if (predicate.apply(e1))
            return e1;
        return defaultValue;
    }

    @Override
    public boolean any(Predicate<E> predicate) {
        checkNotNull(predicate);
        return predicate.apply(e0) || predicate.apply(e1);
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (e0 == null || e1 == null)
            throw new InvalidObjectException("Class does not accept null values");
    }

    public static <E> FunctionalList<E> of(E e, E e1) {
        return new ImmutableFunctionalList2<E>(e, e1);
    }
}
