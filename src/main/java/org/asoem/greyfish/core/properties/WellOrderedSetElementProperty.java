package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.lang.WellOrderedSetElement;

/**
 * User: christoph
 * Date: 01.03.11
 * Time: 16:08
 */
public interface WellOrderedSetElementProperty<T extends Number & Comparable<T>> extends DiscreteProperty<T>, WellOrderedSetElement<T> {
    @Override
    T get();
}
