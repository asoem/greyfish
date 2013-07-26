package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;

import java.util.Collection;

/**
 * User: christoph
 * Date: 15.01.13
 * Time: 18:14
 */
public interface FunctionalCollection<E> extends Collection<E>, Searchable<E> {
    public boolean any(Predicate<E> predicate);
}
