package org.asoem.greyfish.core.space;

import com.google.common.base.Predicate;
import org.asoem.greyfish.utils.space.SpatialObject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;


public interface Space<T, P extends SpatialObject> {
    /**
     * The number of objects in this space
     *
     * @return the number of objects in this space
     */
    int countObjects();

    /**
     * Get the list of objects which have been added to this space
     *
     * @return all objects for this space
     */
    Collection<T> getObjects();

    /**
     * Add the given {@code object} to this space
     *
     * @param object     the object to project
     * @param projection the projection
     * @return {@code true} if the object and it's projection could be added, {@code false} otherwise
     */
    boolean insertObject(T object, P projection);

    /**
     * Remove the given {@code object} from this space
     *
     * @param object the object to remove
     * @return {@code true} if the object could be removed, {@code false} otherwise
     */
    boolean removeObject(T object);

    /**
     * Remove all projections from this space if the satisfy the given predicate
     *
     * @param predicate the predicate to check against
     * @return {@code true} if at least one object was removed, {@code false} otherwise
     */
    boolean removeIf(Predicate<T> predicate);

    boolean isEmpty();

    @Nullable
    P getProjection(T object);

    Map<T, P> asMap();
}
