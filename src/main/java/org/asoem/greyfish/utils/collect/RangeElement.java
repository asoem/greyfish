package org.asoem.greyfish.utils.collect;

import com.google.common.base.Supplier;
import com.google.common.collect.Range;

public interface RangeElement<E extends Number & Comparable<E>> {
    /**
     * @return the value of this element
     */
    E getValue();

    /**
     *
     * @return the {@code Range} that contains this element's value
     */
    Range<E> getRange();
}
