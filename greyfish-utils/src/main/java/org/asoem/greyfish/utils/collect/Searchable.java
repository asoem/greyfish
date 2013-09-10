package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

/**
 * An interface for collections which want to support search operations directly,
 * without the need to use the possibly less efficient static methods in {@link com.google.common.collect.Iterators},
 * {@link com.google.common.collect.Iterables}, {@link com.google.common.collect.Collections2} or {@code Lists}.
 */
public interface Searchable<E> {
    /**
     * Find the first element which satisfies {@code predicate}.
     * @param predicate the predicate to check the element against
     * @return An {@code Optional} holding the element which was found, or {@link Optional#absent()}.
     */
    Optional<E> findFirst(Predicate<? super E> predicate);

    /**
     * Filter all elements by given the {@code predicate}.
     * @param predicate the {@code Predicate} to check the elements against
     * @return All elements which satisfy {@code predicate}
     */
    Iterable<E> filter(Predicate<? super E> predicate);
}
