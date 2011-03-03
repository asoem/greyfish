package org.asoem.greyfish.core.space;

public interface KDTree<T extends MovingObject2D> {
    void rebuild(final Iterable<T> elements);
    Iterable<T> findNeighbours(Location2D p, final double range);
}
