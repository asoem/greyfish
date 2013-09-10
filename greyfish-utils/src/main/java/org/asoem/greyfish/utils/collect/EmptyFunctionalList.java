package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.io.InvalidObjectException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkElementIndex;

/**
* User: christoph
* Date: 10.10.12
* Time: 22:15
*/
class EmptyFunctionalList extends AbstractFunctionalList<Object> implements Serializable {
    private static transient final EmptyFunctionalList INSTANCE = new EmptyFunctionalList();

    private EmptyFunctionalList() {}

    @Override
    public Object get(final int index) {
        checkElementIndex(index, 0);
        throw new AssertionError("unreachable");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Optional<Object> findFirst(final Predicate<? super Object> predicate) {
        return Optional.absent();
    }

    @Override
    public Iterable<Object> filter(final Predicate<? super Object> predicate) {
        return this;
    }

    private Object readResolve() throws InvalidObjectException {
        return INSTANCE;
    }

    private static final long serialVersionUID = 0;

    @SuppressWarnings("unchecked")
    public static <E> FunctionalList<E> instance() {
        return (FunctionalList<E>) INSTANCE;
    }
}
