package org.asoem.greyfish.lang;

import com.google.common.collect.AbstractIterator;

public class ArraysArrayIterator<T> extends AbstractIterator<T> {
    private final T[][] array;

    private int x = 0;
    private int y = 0;

    public ArraysArrayIterator(T[][] array) {
        this.array = array;
    }

    @Override
    protected T computeNext() {

        if (y == array.length)
            return endOfData();


        if (x == array[y].length) {
            x = 0;
            ++y;
            return computeNext();
        }
        else
            return array[y][x++];
    }
}
