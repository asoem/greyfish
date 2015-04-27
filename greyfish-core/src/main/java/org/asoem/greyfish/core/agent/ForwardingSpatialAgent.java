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

package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

abstract class ForwardingSpatialAgent<A extends SpatialAgent<A, C, P, ?>, C extends Context<?, A>, P extends Object2D, AC extends AgentContext<A>>
        extends ForwardingAgent<C, AC>
        implements SpatialAgent<A, C, P, AC> {

    @Override
    protected abstract SpatialAgent<A, C, P, ?> delegate();

    @Override
    public Iterable<A> findNeighbours(final double radius) {
        return delegate().findNeighbours(radius);
    }

    @Override
    public double distance(final double degrees) {
        return delegate().distance(degrees);
    }

    @Override
    public Motion2D getMotion() {
        return delegate().getMotion();
    }

    @Override
    public void setMotion(final Motion2D motion) {
        delegate().setMotion(motion);
    }

    @Override
    public P getProjection() {
        return delegate().getProjection();
    }

    @Override
    public void setProjection(final P projection) {
        delegate().setProjection(projection);
    }

    @Override
    public <T> T ask(final Object message, final Class<T> replyType) {
        return delegate().ask(message, replyType);
    }

    @Override
    public boolean isActive() {
        return delegate().isActive();
    }

    @Override
    public void activate(final C context) {
        delegate().activate(context);
    }
}
