package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.space.Space2D;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 10:51
 */
public abstract class ForwardingSpatialSimulation<A extends Agent<A, ?, ?>, Z extends Space2D<A, ?>> extends ForwardingSimulation<A> implements SpatialSimulation<A, Z> {

    @Override
    protected abstract SpatialSimulation<A, Z> delegate();

    @Override
    public Iterable<A> findNeighbours(A agent, double distance) {
        return delegate().findNeighbours(agent, distance);
    }

    @Override
    public Z getSpace() {
        return delegate().getSpace();
    }
}
