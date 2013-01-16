package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;

import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkElementIndex;

/**
* User: christoph
* Date: 10.10.12
* Time: 22:15
*/
class EmptyFunctionalList extends AbstractList<Object> implements Serializable, FunctionalList<Object> {
    private static transient final EmptyFunctionalList INSTANCE = new EmptyFunctionalList();

    private EmptyFunctionalList() {}

    @Override
    public Object get(int index) {
        checkElementIndex(index, 0);
        throw new AssertionError("unreachable");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Object find(Predicate<? super Object> predicate) throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    @Override
    public Object find(Predicate<? super Object> predicate, Object defaultValue) {
        return defaultValue;
    }

    @Override
    public Iterable<Object> filter(Predicate<? super Object> predicate) {
        return this;
    }

    private Object readResolve() throws InvalidObjectException {
        return INSTANCE;
    }

    private static final long serialVersionUID = 0;

    public static <E> FunctionalList<E> instance() {
        return (FunctionalList<E>) INSTANCE;
    }
}
