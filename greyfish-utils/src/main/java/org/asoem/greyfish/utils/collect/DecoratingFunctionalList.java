package org.asoem.greyfish.utils.collect;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

public final class DecoratingFunctionalList<E> extends AbstractFunctionalList<E> implements Serializable {

    private final List<E> list;

    @SuppressWarnings("unchecked")
    public DecoratingFunctionalList(final List<? extends E> list) {
        this.list = (List<E>) list;
    }

    public static <E> FunctionalList<E> decorate(final List<? extends E> list) {
        return new DecoratingFunctionalList<E>(list);
    }

    private void readObject(final ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (list == null) {
            throw new InvalidObjectException("delegate must not be null values");
        }
    }

    private static final long serialVersionUID = 0;

    @Override
    public E get(final int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }
}
