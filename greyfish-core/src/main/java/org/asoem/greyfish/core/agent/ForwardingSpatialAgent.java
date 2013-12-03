package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Set;

abstract class ForwardingSpatialAgent<A extends SpatialAgent<A, P, C>, P extends Object2D, C extends BasicSimulationContext<? extends SpatialSimulation2D<A, ?>, A>>
        extends ForwardingAgent<A, C>
        implements SpatialAgent<A, P, C> {

    @Override
    protected abstract SpatialAgent<A, P, C> delegate();

    @Override
    public Iterable<A> findNeighbours(final double radius) {
        return delegate().findNeighbours(radius);
    }

    @Override
    public double distance(final double degrees) {
        return delegate().distance(degrees);
    }

    @Override
    public Motion2D getMotion() {
        return delegate().getMotion();
    }

    @Override
    public void setMotion(final Motion2D motion) {
        delegate().setMotion(motion);
    }

    @Override
    public P getProjection() {
        return delegate().getProjection();
    }

    @Override
    public void setProjection(final P projection) {
        delegate().setProjection(projection);
    }

    @Override
    public void reproduce(final Chromosome chromosome) {
        delegate().reproduce(chromosome);
    }

    @Override
    public void setParents(final Set<Integer> parents) {
        delegate().setParents(parents);
    }
}
