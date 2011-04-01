package org.asoem.greyfish.lang;

import com.google.common.base.Preconditions;

public class MutableWellOrderedSetElement<T extends Number & Comparable<T>> implements WellOrderedSetElement<T> {

    private final T min;
    private final T max;

    private T value;

    public MutableWellOrderedSetElement(T min, T max, T value) {
        Preconditions.checkNotNull(min);
        Preconditions.checkNotNull(max);
        Preconditions.checkArgument(min.compareTo(max) <= 0);
        this.min = min;
        this.max = max;
        set(value);
    }

    public void set(T value) {
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(value.compareTo(min) >= 0);
        Preconditions.checkArgument(value.compareTo(max) <= 0);
        this.value = value;
    }

    @Override
    public T getUpperBound() {
        return max;
    }

    @Override
    public T getLowerBound() {
        return min;
    }

    @Override
    public T get() {
        return value;
    }
}
