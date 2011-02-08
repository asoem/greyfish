package org.asoem.greyfish.core.space;

public interface KDTree<T extends Object2DInterface> {
    void rebuild(final Iterable<T> elements);
    Iterable<T> findNeighbours(Location2DInterface p, final double range);
}
