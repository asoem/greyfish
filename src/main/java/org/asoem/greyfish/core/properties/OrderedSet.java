package org.asoem.greyfish.core.properties;

public interface OrderedSet<E extends Comparable<E>> {
    public E getUpperBound();
    public E getLowerBound();
}
