package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 10:51
 */
public abstract class ForwardingSpatialSimulation<A extends Agent<A, ?>, Z extends Space2D<A, P>, P extends Object2D> extends ForwardingSimulation<A> implements SpatialSimulation<A, Z, P> {

    @Override
    protected abstract SpatialSimulation<A, Z, P> delegate();

    @Override
    public Iterable<A> findNeighbours(A agent, double distance) {
        return delegate().findNeighbours(agent, distance);
    }

    @Override
    public Z getSpace() {
        return delegate().getSpace();
    }

    @Override
    public double distance(A agent, double degrees) {
        return delegate().distance(agent, degrees);
    }

    @Override
    public void createAgent(Population population, P projection) {
        delegate().createAgent(population, projection);
    }
}
