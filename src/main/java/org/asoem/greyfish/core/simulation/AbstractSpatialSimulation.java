package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.space.Space2D;

import java.util.Collection;

/**
 * User: christoph
 * Date: 03.10.12
 * Time: 22:08
 */
public abstract class AbstractSpatialSimulation<A extends Agent<A, ?>, Z extends Space2D<A, ?>> extends AbstractSimulation<A> implements SpatialSimulation<A, Z> {

    @Override
    public Iterable<A> findNeighbours(A agent, double distance) {
        return getSpace().getVisibleNeighbours(agent, distance);
    }

    @Override
    public Collection<A> getAgents() {
        return getSpace().getObjects();
    }

    @Override
    public int countAgents() {
        return getSpace().countObjects();
    }
}
