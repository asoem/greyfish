package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class UnrolledList4<E> extends DelegatingImmutableFunctionalList<E> implements Serializable {

    private final List<E> delegate;

    UnrolledList4(final Iterable<? extends E> elements) {
        checkNotNull(elements);
        this.delegate = ImmutableList.copyOf(elements);
        checkArgument(delegate.size() == 4);
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    @Override
    public Optional<E> findFirst(final Predicate<? super E> predicate) {
        checkNotNull(predicate, "Predicate is null");
        if (predicate.apply(delegate.get(0))) {
            return Optional.of(delegate.get(0));
        } else if (predicate.apply(delegate.get(1))) {
            return Optional.of(delegate.get(1));
        } else if (predicate.apply(delegate.get(2))) {
            return Optional.of(delegate.get(2));
        } else if (predicate.apply(delegate.get(3))) {
            return Optional.of(delegate.get(3));
        } else {
            return Optional.absent();
        }
    }
}
