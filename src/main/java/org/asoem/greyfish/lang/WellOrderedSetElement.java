package org.asoem.greyfish.lang;

import com.google.common.base.Supplier;

public interface WellOrderedSetElement<E extends Number & Comparable<E>> extends Supplier<E> {
    E getUpperBound();
    E getLowerBound();
    @Override
    E get();
}
