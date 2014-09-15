package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.environment.SpatialEnvironment2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * Base implementation of a spatial agent
 */
public abstract class AbstractSpatialAgent<A extends SpatialAgent<A, BasicContext<S, A>, P, ?>, S extends SpatialEnvironment2D<A, ?>, P extends Object2D, AC extends AgentContext<A>>
        extends AbstractAgent<A, BasicContext<S, A>, AC> implements SpatialAgent<A, BasicContext<S, A>, P, AC> {

    @Override
    public double distance(final double degrees) {
        return getContext().get().getEnvironment().distance(self(), degrees);
    }

    @Override
    public Iterable<A> findNeighbours(final double radius) {
        return getContext().get().getEnvironment().findNeighbours(self(), radius);
    }
}
