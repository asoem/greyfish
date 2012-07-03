package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.Location2D;
import org.asoem.greyfish.utils.space.MovingProjectable2D;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 18:56
 */
public interface Space2D<T extends MovingProjectable2D> {

    int countObjects();

    boolean contains(Location2D location);

    Iterable<T> getObjects();

    void addObject(T projectable);

    boolean removeObject(T object);

    void moveObject(T object2d);

    /**
     * @param point the locatable of the search point
     * @param range the radius of the circle around {@code locatable}
     * @return evaluates objects whose location in this space
     *         intersects with the circle defined by {@code locatable} and {@code range}
     */
    Iterable<T> findObjects(Location2D point, double range);
}
