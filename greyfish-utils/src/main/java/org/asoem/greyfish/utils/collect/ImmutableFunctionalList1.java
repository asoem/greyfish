package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

final class ImmutableFunctionalList1<E> extends ImmutableFunctionalList<E> {
    private final E e0;

    ImmutableFunctionalList1(final E e0) {
        this.e0 = checkNotNull(e0);
    }

    @Override
    public E get(final int index) {
        checkElementIndex(index, size());
        return e0;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Iterable<E> filter(final Predicate<? super E> predicate) {
        return checkNotNull(predicate).apply(e0) ? this : ImmutableList.<E>of();
    }

    @Override
    public boolean any(final Predicate<E> predicate) {
        return checkNotNull(predicate).apply(e0);
    }

    @Override
    public Optional<E> findFirst(final Predicate<? super E> predicate) {
        return checkNotNull(predicate).apply(e0) ? Optional.of(e0) : Optional.<E>absent();
    }
}
