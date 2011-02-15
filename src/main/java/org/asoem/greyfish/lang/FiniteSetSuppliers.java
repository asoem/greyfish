package org.asoem.greyfish.lang;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * User: christoph
 * Date: 15.02.11
 * Time: 13:46
 */
public class FiniteSetSuppliers {

    public static <T> FiniteSetSupplier<T> of(final T value) {
        return new FiniteSetSupplier<T>() {

            @Override
            public Set<T> getSet() {
                return Sets.newHashSet(value);
            }

            @Override
            public T get() {
                return value;
            }
        };
    }
}
