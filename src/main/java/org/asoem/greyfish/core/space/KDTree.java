package org.asoem.greyfish.core.space;

public interface KDTree<T extends Object2D> {
    void rebuild(final Iterable<T> elements);
    Iterable<T> findNeighbours(Coordinates2D p, final double range);
}
