package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.lang.OrderedSet;

/**
 * User: christoph
 * Date: 01.03.11
 * Time: 16:08
 */
public interface OrderedSetProperty<T extends Comparable<T>> extends DiscreteProperty<T>, OrderedSet<T> {
    @Override
    T get();
}
