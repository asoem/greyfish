package org.asoem.greyfish.utils.collect;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* User: christoph
* Date: 10.10.12
* Time: 22:14
*/
class ForwardingFunctionalList<E> extends AbstractFunctionalList<E> implements Serializable, FunctionalList<E> {

    private final List<E> delegate;

    public ForwardingFunctionalList(List<E> elements) {
        this.delegate = checkNotNull(elements);
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (delegate == null)
            throw new InvalidObjectException("delegate must not be null values");
    }

    private static final long serialVersionUID = 0;

    @Override
    public E get(int index) {
        return delegate.get(index);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    public E set(int index, E element) {
        return delegate.set(index, element);
    }

    public void add(int index, E element) {
        delegate.add(index, element);
    }

    @Override
    public E remove(int index) {
        return delegate.remove(index);
    }
}
