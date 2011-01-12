package org.asoem.greyfish.core.space;

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 12.01.11
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
public interface KDTreeAdaptor<T extends Object2DInterface> {
    void rebuild(final Iterable<T> elements);
    Iterable<T> findNeighbours(Location2DInterface p, final double range);
}
