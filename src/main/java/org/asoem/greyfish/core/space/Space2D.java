package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.MovingProjectable2D;

import java.util.List;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 18:56
 */
public interface Space2D<T extends MovingProjectable2D> {

    int countObjects();

    boolean contains(double x, double y);

    List<T> getObjects();

    void insertObject(T projectable, double x, double y, double orientation);

    boolean removeObject(T object);

    void moveObject(T object2d);

    /**
     *
     * @param x
     * @param y
     *@param range the radius of the circle around {@code locatable}  @return evaluates objects whose location in this space
     *         intersects with the circle defined by {@code locatable} and {@code range}
     */
    Iterable<T> findObjects(double x, double y, double range);
}
