package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Collection;

/**
 * User: christoph
 * Date: 03.10.12
 * Time: 22:08
 */
public abstract class Abstract2DSimulation<A extends SpatialAgent<A, ?, P>, Z extends Space2D<A, P>, P extends Object2D> extends AbstractSimulation<A> implements SpatialSimulation2D<A, Z, P> {

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
