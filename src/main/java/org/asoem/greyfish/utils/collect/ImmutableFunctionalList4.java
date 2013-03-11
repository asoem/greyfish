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
class ImmutableFunctionalList4<E> extends ImmutableFunctionalList<E> implements Serializable, FunctionalList<E> {

    final private E e0;
    final private E e1;
    final private E e2;
    final private E e3;

    ImmutableFunctionalList4(E e0, E e1, E e2, E e3) {
        this.e0 = checkNotNull(e0);
        this.e1 = checkNotNull(e1);
        this.e2 = checkNotNull(e2);
        this.e3 = checkNotNull(e3);
    }

    @Override
    public E get(int index) {
        switch (index) {
            case 0: return e0;
            case 1: return e1;
            case 2: return e2;
            case 3: return e3;
            default: checkPositionIndex(index, size()); throw new AssertionError("unreachable");
        }
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public E find(Predicate<? super E> predicate) {
        checkNotNull(predicate, "Predicate is null");
        if (predicate.apply(e0))
            return e0;
        else if (predicate.apply(e1))
            return e1;
        else if (predicate.apply(e2))
            return e2;
        else if (predicate.apply(e3))
            return e3;
        else
            throw new NoSuchElementException("No element was found matching the given predicate: " + predicate);
    }

    @Override
    public E find(Predicate<? super E> predicate, E defaultValue) {
        checkNotNull(predicate, "Predicate is null");
        if (predicate.apply(e0))
            return e0;
        else if (predicate.apply(e1))
            return e1;
        else if (predicate.apply(e2))
            return e2;
        else if (predicate.apply(e3))
            return e3;
        else
            return defaultValue;
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (e0 == null || e1 == null || e2 == null)
            throw new InvalidObjectException("Class does not accept null values");
    }

    public static <E> FunctionalList<E> of(E e0, E e1, E e2, E e3) {
        return new ImmutableFunctionalList4<E>(e0, e1, e2, e3);
    }
}
