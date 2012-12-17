package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 04.12.12
 * Time: 15:19
 */
public abstract class ForwardingSpatialAgent<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation<A, ?, P>, P extends Object2D> extends ForwardingAgent<A, S> implements SpatialAgent<A, S, P> {

    @Override
    protected abstract SpatialAgent<A, S, P> delegate();

    @Override
    public Iterable<A> findNeighbours(double radius) {
        return delegate().findNeighbours(radius);
    }

    @Override
    public double distance(A agent, double degrees) {
        return delegate().distance(agent, degrees);
    }

    @Override
    public Motion2D getMotion() {
        return delegate().getMotion();
    }

    @Override
    public void setMotion(Motion2D motion) {
        delegate().setMotion(motion);
    }

    @Override
    public P getProjection() {
        return delegate().getProjection();
    }

    @Override
    public void setProjection(P projection) {
        delegate().setProjection(projection);
    }
}
