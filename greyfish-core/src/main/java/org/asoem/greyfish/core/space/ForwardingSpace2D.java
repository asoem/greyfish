package org.asoem.greyfish.core.space;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Collection;
import java.util.Map;


public abstract class ForwardingSpace2D<O, P extends Object2D> extends ForwardingObject implements Space2D<O, P> {
    @Override
    protected abstract Space2D<O, P> delegate();

    @Override
    public int countObjects() {
        return delegate().countObjects();
    }

    @Override
    public boolean contains(final double x, final double y) {
        return delegate().contains(x, y);
    }

    @Override
    public Collection<O> getObjects() {
        return delegate().getObjects();
    }

    @Override
    public boolean removeObject(final O object) {
        return delegate().removeObject(object);
    }

    @Override
    public void moveObject(final O object2d, final Motion2D motion2D) {
        delegate().moveObject(object2d, motion2D);
    }

    @Override
    public Iterable<O> findObjects(final double x, final double y, final double radius) {
        return delegate().findObjects(x, y, radius);
    }

    @Override
    public boolean insertObject(final O object, final P projection) {
        return delegate().insertObject(object, projection);
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public boolean removeIf(final Predicate<O> predicate) {
        return delegate().removeIf(predicate);
    }

    @Override
    public Iterable<O> getVisibleNeighbours(final O object, final double radius) {
        return delegate().getVisibleNeighbours(object, radius);
    }

    @Override
    public double width() {
        return delegate().width();
    }

    @Override
    public double height() {
        return delegate().height();
    }

    @Override
    public P getProjection(final O object) {
        return delegate().getProjection(object);
    }

    @Override
    public Map<O, P> asMap() {
        return delegate().asMap();
    }

    @Override
    public double distance(final O agent, final double degrees) {
        return delegate().distance(agent, degrees);
    }
}
