package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* User: christoph
* Date: 10.10.12
* Time: 22:14
*/
class ForwardingAugmentedList<E> extends ForwardingList<E> implements AugmentedList<E>, Serializable {

    private final List<E> delegate;

    public ForwardingAugmentedList(List<E> elements) {
        this.delegate = checkNotNull(elements);
    }

    @Override
    public E find(Predicate<? super E> predicate) throws NoSuchElementException {
        return Iterables.find(delegate, predicate);
    }

    @Override
    public E find(Predicate<? super E> predicate, E defaultValue) {
        return Iterables.find(delegate, predicate, defaultValue);
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (delegate == null)
            throw new InvalidObjectException("delegate must not be null values");
    }

    private static final long serialVersionUID = 0;
}
