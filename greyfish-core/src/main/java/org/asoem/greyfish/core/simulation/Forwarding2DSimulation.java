package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 10:51
 */
public abstract class Forwarding2DSimulation<A extends SpatialAgent<A, ?, P>, Z extends Space2D<A, P>, P extends Object2D> extends ForwardingSimulation<A> implements SpatialSimulation2D<A, Z> {

    @Override
    protected abstract SpatialSimulation2D<A, Z> delegate();

    @Override
    public Iterable<A> findNeighbours(final A agent, final double distance) {
        return delegate().findNeighbours(agent, distance);
    }

    @Override
    public Z getSpace() {
        return delegate().getSpace();
    }

    @Override
    public double distance(final A agent, final double degrees) {
        return delegate().distance(agent, degrees);
    }
}
