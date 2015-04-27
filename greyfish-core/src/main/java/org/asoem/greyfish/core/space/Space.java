/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
