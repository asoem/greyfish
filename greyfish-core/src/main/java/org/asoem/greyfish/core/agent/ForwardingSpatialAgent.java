package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

abstract class ForwardingSpatialAgent<A extends SpatialAgent<A, C, P, ?>, C extends Context<?, A>, P extends Object2D, AC extends AgentContext<A>>
        extends ForwardingAgent<C, AC>
        implements SpatialAgent<A, C, P, AC> {

    @Override
    protected abstract SpatialAgent<A, C, P, ?> delegate();

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
    public <T> T ask(final Object message, final Class<T> replyType) {
        return delegate().ask(message, replyType);
    }

    @Override
    public boolean isActive() {
        return delegate().isActive();
    }

    @Override
    public void activate(final C context) {
        delegate().activate(context);
    }
}
