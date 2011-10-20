package org.asoem.greyfish.utils.collect;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

public class MutableRangeElement<T extends Number & Comparable<T>> implements RangeElement<T> {

    private final Range<T> range;

    private T value;

    public MutableRangeElement(T min, T max, T value) {
        range = Ranges.closed(min, max);
        set(value);
    }

    public void set(T value) {
        Preconditions.checkArgument(range.contains(value));
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public Range<T> getRange() {
        return range;
    }
}
