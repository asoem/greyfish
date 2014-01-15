package org.asoem.greyfish.utils.collect;

import java.util.List;

abstract class DelegatingImmutableFunctionalList<E> extends ImmutableFunctionalList<E> {

    protected abstract List<E> delegate();

    @Override
    public final E get(final int index) {
        return delegate().get(index);
    }

    @Override
    public final int size() {
        return delegate().size();
    }
}
