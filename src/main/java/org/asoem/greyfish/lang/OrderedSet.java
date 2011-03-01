package org.asoem.greyfish.lang;

public interface OrderedSet<E extends Comparable<E>> {
    public E getUpperBound();
    public E getLowerBound();
}
