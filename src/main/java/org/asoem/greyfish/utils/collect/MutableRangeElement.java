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

    public T getValue() {
        return value;
    }

    @Override
    public Range<T> getRange() {
        return range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutableRangeElement that = (MutableRangeElement) o;

        return range.equals(that.range) && value.equals(that.value);

    }

    @Override
    public int hashCode() {
        int result = range.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
