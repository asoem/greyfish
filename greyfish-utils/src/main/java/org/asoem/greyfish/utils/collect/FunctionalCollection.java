package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;

import java.util.Collection;

/**
 * A {@link Collection} which supports functional style operations.
 */
public interface FunctionalCollection<E> extends Collection<E>, Searchable<E> {
    /**
     * Check if at least one of the elements satisfies the given {@code predicate}.
     * @param predicate the {@code Predicate} to check against
     * @return {@code true} if at least one element satisfies {@code predicate}, {@code false} otherwise
     */
    boolean any(Predicate<E> predicate);
}
