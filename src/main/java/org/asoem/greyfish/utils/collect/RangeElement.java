package org.asoem.greyfish.utils.collect;

import com.google.common.collect.Range;

public interface RangeElement<E extends Number & Comparable<E>> {

    /**
     *
     * @return the {@code Range} that contains this element's value
     */
    Range<E> getRange();
}
