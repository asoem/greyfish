package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is a {@link org.asoem.greyfish.utils.collect.FunctionalList FunctionalList} implementation for lists of size 3.
 * The predictable size allows some operations to unroll the list for performance optimizations.
 *
 * @param <E> the element type
 */
final class UnrolledList3<E> extends DelegatingImmutableFunctionalList<E> implements Serializable {

    private final List<E> delegate;

    UnrolledList3(final Iterable<? extends E> elements) {
        checkNotNull(elements);
        this.delegate = ImmutableList.copyOf(elements);
        checkArgument(delegate.size() == 3);
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    @Override
    public Optional<E> findFirst(final Predicate<? super E> predicate) {
        checkNotNull(predicate, "Predicate is null");
        final E e0 = delegate.get(0);
        if (predicate.apply(e0)) {
            return Optional.of(e0);
        }
        final E e1 = delegate.get(1);
        if (predicate.apply(e1)) {
            return Optional.of(e1);
        }
        final E e2 = delegate.get(2);
        if (predicate.apply(e2)) {
            return Optional.of(e2);
        }
        return Optional.absent();
    }
}
