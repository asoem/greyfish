package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 11.12.12
 * Time: 10:13
 */
public abstract class AbstractSpatialAgent<A extends Agent<A, S>, S extends SpatialSimulation<A, ?>, P extends Object2D> extends AbstractAgent<A, S> implements SpatialAgent<A, S, P> {

    @Override
    public double distance(A agent, double degrees) {
        return simulation().distance(agent, degrees);
    }

    @Override
    public Iterable<A> findNeighbours(double radius) {
        return simulation().findNeighbours(self(), radius);
    }
}
