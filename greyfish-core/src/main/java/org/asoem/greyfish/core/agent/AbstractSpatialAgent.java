package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * Base implementation of a spatial agent
 */
public abstract class AbstractSpatialAgent<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, ?>, P extends Object2D>
        extends AbstractAgent<A, S> implements SpatialAgent<A, S, P> {

    @Override
    public double distance(final double degrees) {
        return getContext().get().getSimulation().distance(self(), degrees);
    }

    @Override
    public Iterable<A> findNeighbours(final double radius) {
        return getContext().get().getSimulation().findNeighbours(self(), radius);
    }
}
