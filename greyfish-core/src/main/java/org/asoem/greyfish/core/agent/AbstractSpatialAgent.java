package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 11.12.12
 * Time: 10:13
 */
public abstract class AbstractSpatialAgent<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, ?>, P extends Object2D> extends AbstractAgent<A, S> implements SpatialAgent<A, S, P> {

    @Override
    public double distance(final A agent, final double degrees) {
        return simulation().distance(agent, degrees);
    }

    @Override
    public Iterable<A> findNeighbours(final double radius) {
        return simulation().findNeighbours(self(), radius);
    }
}
