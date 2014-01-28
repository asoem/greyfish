package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.space.Space2D;

/**
 * Basic class for implementing 2D simulations.
 */
public abstract class Abstract2DSimulation<A extends SpatialAgent<A, ?, ?, ?>, Z extends Space2D<A, ?>>
        extends AbstractSimulation<A> implements SpatialSimulation2D<A, Z> {

    @Override
    public Iterable<A> findNeighbours(final A agent, final double distance) {
        return getSpace().getVisibleNeighbours(agent, distance);
    }

    @Override
    public Iterable<A> getActiveAgents() {
        return getSpace().getObjects();
    }

    @Override
    public int countAgents() {
        return getSpace().countObjects();
    }

    /**
     * Remove agent from this {@code Simulation}
     *
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    public abstract void removeAgent(A agent);
}
