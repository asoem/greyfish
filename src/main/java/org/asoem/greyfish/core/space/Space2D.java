package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.Location2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Projectable;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 18:56
 */
public interface Space2D<T extends Projectable<Object2D>> {

    int countObjects();
    boolean contains(Location2D location);
    Iterable<T> getObjects();
    void addObject(T projectable, Object2D projection);
    boolean removeObject(T object);
    Object2D moveObject(T object2d, Motion2D motion);

    /**
     * @param point the locatable of the search point
     * @param range the radius of the circle around {@code locatable}
     * @return evaluates objects whose location in this space
     * intersects with the circle defined by {@code locatable} and {@code range}
     */
    Iterable<T> findObjects(Location2D point, double range);
}
