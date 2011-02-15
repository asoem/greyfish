package org.asoem.greyfish.lang;

import com.google.common.base.Supplier;

import java.util.Set;

/**
 * User: christoph
 * Date: 15.02.11
 * Time: 13:44
 */
public interface FiniteSetSupplier<T> extends Supplier<T> {
    public Set<T> getSet();
}
