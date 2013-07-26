package org.asoem.greyfish.utils.collect;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

/**
* User: christoph
* Date: 10.02.13
* Time: 13:46
*/
public class DecoratingFunctionalList<E> extends AbstractFunctionalList<E> implements Serializable {

    private final List<E> list;

    public DecoratingFunctionalList(final List<E> list) {
        this.list = list;
    }

    public static <E> FunctionalList<E> decorate(final List<E> list) {
        return new DecoratingFunctionalList<E>(list);
    }

    private void readObject(final ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (list == null)
            throw new InvalidObjectException("delegate must not be null values");
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
