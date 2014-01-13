package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * Base implementation of a spatial agent
 */
public abstract class AbstractSpatialAgent<A extends SpatialAgent<A, BasicSimulationContext<S, A>, P, ?>, S extends SpatialSimulation2D<A, ?>, P extends Object2D, AC extends AgentContext<A>>
        extends AbstractAgent<A, BasicSimulationContext<S, A>, AC> implements SpatialAgent<A, BasicSimulationContext<S, A>, P, AC> {

    @Override
    public double distance(final double degrees) {
        return getContext().get().getSimulation().distance(self(), degrees);
    }

    @Override
    public Iterable<A> findNeighbours(final double radius) {
        return getContext().get().getSimulation().findNeighbours(self(), radius);
    }
}
