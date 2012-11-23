package org.asoem.greyfish.core.space;

import com.google.common.base.Predicate;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Collection;
import java.util.Map;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 18:56
 */
public interface Space2D<T, P extends Object2D> {

    /**
     * The number of objects in this space
     * @return the number of objects in this space
     */
    int countObjects();

    /**
     * Tests if the point given by the coordinates {@code x} and {@code y} lies inside this space
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return {@code true}, if th point is within the boundaries of this space, {@code false} otherwise
     */
    boolean contains(double x, double y);

    /**
     * Get the list of objects which have been added to this space
     * @return all objects for this space
     */
    Collection<T> getObjects();

    /**
     * Add the given {@code object} to this space
     * @param object the object to project
     * @param projection the projection
     * @return {@code true} if the object and it's projection could be added, {@code false} otherwise
     */
    boolean insertObject(T object, P projection);

    /**
     * Remove the given {@code object} from this space
     * @param object the object to remove
     * @return {@code true} if the object could be removed, {@code false} otherwise
     */
    boolean removeObject(T object);

    /**
     * Remove all projections from this space if the satisfy the given predicate
     * @param predicate the predicate to check against
     * @return {@code true} if at least one object was removed, {@code false} otherwise
     */
    boolean removeIf(Predicate<T> predicate);

    /**
     * Move given object using it's motion
     *
     * @param object2d the object to move
     * @param motion2D the motion defining the move
     * @see org.asoem.greyfish.utils.space.MovingProjectable2D#getMotion()
     */
    void moveObject(T object2d, Motion2D motion2D);

    /**
     * Find all objects in this space with are contained in the circle
     * defined by the center point at {@code x}, {@code y} and the {@code radius}
     * @param x the x coordinate of the center point
     * @param y the y coordinate of the center point
     * @param radius the radius of the circle
     */
    Iterable<T> findObjects(double x, double y, double radius);

    /**
     * Find all neighbours which are located inside the circle around {@code object} with given {@code radius}
     * and are visible by the given object.
     * Visibility is implementation dependent.
     * @param object the object which acts as the center point for the search
     * @param radius the radius around the given object
     * @return all objects which are visible by the given object at a distance less than or equal to the given {@code radius}
     */
    Iterable<T> getVisibleNeighbours(T object, double radius);

    boolean isEmpty();

    double width();

    double height();

    P getProjection(T object);

    Map<T, P> asMap();

    double distance(T agent, double degrees);
}