package org.asoem.greyfish.lang;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.Iterator;


public final class Comparables {
    public static <T extends Comparable<T>> boolean areInOrder(T ... others) {
        return areInOrder(ImmutableList.<T>builder().addAll(Arrays.asList(others)).build());
    }

    public static <T extends Comparable<T>> boolean areInOrder(final Iterable<T> list) {
        Preconditions.checkNotNull(list);
        if (Iterables.size(list) < 2)
            return true;
        Iterator<T> iter = list.iterator();
        T head = iter.next();
        Iterable<T> tail = ImmutableList.copyOf(iter);
        return areInOrder(head, tail);
    }

    private static <T extends Comparable<T>> boolean areInOrder(final T head, final Iterable<T> tail) {
        assert head != null;
        assert tail != null;
        Iterator<T> iter = tail.iterator();
        if ( ! iter.hasNext())
            return true;
        T next = iter.next();
        ImmutableList<T> newTail = ImmutableList.copyOf(iter);
        return head.compareTo(next) <= 0 && areInOrder(next, newTail);
    }
}
