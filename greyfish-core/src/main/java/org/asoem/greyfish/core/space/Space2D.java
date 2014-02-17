package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.MovingProjectable2D;
import org.asoem.greyfish.utils.space.Object2D;


public interface Space2D<T, P extends Object2D> extends Space<T, P> {

    /**
     * Tests if the point given by the coordinates {@code x} and {@code y} lies inside this space
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return {@code true}, if th point is within the boundaries of this space, {@code false} otherwise
     */
    boolean contains(double x, double y);

    /**
     * Move given object using it's motion
     *
     * @param object2d the object to move
     * @param motion2D the motion defining the move
     * @see MovingProjectable2D#getMotion()
     */
    void moveObject(T object2d, Motion2D motion2D);

    /**
     * Find all objects in this space with are contained in the circle defined by the center point at {@code x}, {@code
     * y} and the {@code radius}
     *
     * @param x      the x coordinate of the center point
     * @param y      the y coordinate of the center point
     * @param radius the radius of the circle
     */
    Iterable<T> findObjects(double x, double y, double radius);

    /**
     * Find all neighbours which are located inside the circle around {@code object} with given {@code radius} and are
     * visible by the given object. Visibility is implementation dependent.
     *
     * @param object the object which acts as the center point for the search
     * @param radius the radius around the given object
     * @return all objects which are visible by the given object at a distance less than or equal to the given {@code
     * radius}
     */
    Iterable<T> getVisibleNeighbours(T object, double radius);

    double width();

    double height();

    double distance(T agent, double degrees);
}