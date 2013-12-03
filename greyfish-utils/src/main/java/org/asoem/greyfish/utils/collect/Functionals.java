package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * This is a utility class which has the same methods as {@link Iterables}
 * but is aware of the direct implementation in functional collections such a {@link org.asoem.greyfish.utils.collect.FunctionalCollection}.
 * That means this class decides weather to delegates to the iterables native methods or the helper methods in {@link Iterables}
 * based on the type of the subject.
 */
public class Functionals {
    private Functionals() {
    }

    @SuppressWarnings("unchecked") // safe cast
    public static <E> Optional<E> tryFind(final Iterable<E> iterable, final Predicate<? super E> predicate) {
        if (iterable instanceof Searchable) {
            return ((Searchable<E>) iterable).findFirst(predicate);
        } else {
            return Iterables.tryFind(iterable, predicate);
        }
    }
}
