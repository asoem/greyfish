package org.asoem.greyfish.core.environment;

import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.space.Space2D;

/**
 * Basic class for implementing 2D simulations.
 */
public abstract class Abstract2DEnvironment<A extends SpatialAgent<A, ?, ?, ?>, Z extends Space2D<A, ?>>
        extends AbstractEnvironment<A> implements SpatialEnvironment2D<A, Z> {

    @Override
    public final Iterable<A> findNeighbours(final A agent, final double distance) {
        return getSpace().getVisibleNeighbours(agent, distance);
    }

    @Override
    public final Iterable<A> getActiveAgents() {
        return getSpace().getObjects();
    }

    @Override
    public final int countAgents() {
        return getSpace().countObjects();
    }

    /**
     * Remove agent from this {@code Simulation}
     *
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    public abstract void removeAgent(A agent);
}
