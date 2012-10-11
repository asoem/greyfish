package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;

/**
* User: christoph
* Date: 10.10.12
* Time: 22:14
*/
class RegularTinyList<E> extends ForwardingList<E> implements TinyList<E>, Serializable {

    private final List<E> delegate;

    RegularTinyList(Iterable<E> elements) {
        this.delegate = ImmutableList.copyOf(elements);
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

    private static final long serialVersionUID = 0;
}
