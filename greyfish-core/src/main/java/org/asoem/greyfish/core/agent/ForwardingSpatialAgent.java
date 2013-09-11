package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 04.12.12
 * Time: 15:19
 */
public abstract class ForwardingSpatialAgent<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, ?>, P extends Object2D> extends ForwardingAgent<A, S> implements SpatialAgent<A, S, P> {

    @Override
    protected abstract SpatialAgent<A, S, P> delegate();

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
