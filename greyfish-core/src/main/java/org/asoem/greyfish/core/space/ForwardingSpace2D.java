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
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Collection;
import java.util.Map;


public abstract class ForwardingSpace2D<O, P extends Object2D> extends ForwardingObject implements Space2D<O, P> {
    @Override
    protected abstract Space2D<O, P> delegate();

    @Override
    public int countObjects() {
        return delegate().countObjects();
    }

    @Override
    public boolean contains(final double x, final double y) {
        return delegate().contains(x, y);
    }

    @Override
    public Collection<O> getObjects() {
        return delegate().getObjects();
    }

    @Override
    public boolean removeObject(final O object) {
        return delegate().removeObject(object);
    }

    @Override
    public void moveObject(final O object2d, final Motion2D motion2D) {
        delegate().moveObject(object2d, motion2D);
    }

    @Override
    public Iterable<O> findObjects(final double x, final double y, final double radius) {
        return delegate().findObjects(x, y, radius);
    }

    @Override
    public boolean insertObject(final O object, final P projection) {
        return delegate().insertObject(object, projection);
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public boolean removeIf(final Predicate<O> predicate) {
        return delegate().removeIf(predicate);
    }

    @Override
    public Iterable<O> getVisibleNeighbours(final O object, final double radius) {
        return delegate().getVisibleNeighbours(object, radius);
    }

    @Override
    public double width() {
        return delegate().width();
    }

    @Override
    public double height() {
        return delegate().height();
    }

    @Override
    public P getProjection(final O object) {
        return delegate().getProjection(object);
    }

    @Override
    public Map<O, P> asMap() {
        return delegate().asMap();
    }

    @Override
    public double distance(final O agent, final double degrees) {
        return delegate().distance(agent, degrees);
    }
}
