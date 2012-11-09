package org.asoem.greyfish.core.space;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.utils.space.MovingProjectable2D;

import java.util.List;

/**
 * User: christoph
 * Date: 09.11.12
 * Time: 14:41
 */
public abstract class ForwardingSpace2D<O extends MovingProjectable2D> extends ForwardingObject implements Space2D<O> {
    @Override
    protected abstract Space2D<O> delegate();

    @Override
    public int countObjects() {
        return delegate().countObjects();
    }

    @Override
    public boolean contains(double x, double y) {
        return delegate().contains(x, y);
    }

    @Override
    public List<O> getObjects() {
        return delegate().getObjects();
    }

    @Override
    public boolean insertObject(O projectable, double x, double y, double orientation) {
        return delegate().insertObject(projectable, x, y, orientation);
    }

    @Override
    public boolean removeObject(O object) {
        return delegate().removeObject(object);
    }

    @Override
    public void moveObject(O object2d) {
        delegate().moveObject(object2d);
    }

    @Override
    public Iterable<O> findObjects(double x, double y, double radius) {
        return delegate().findObjects(x, y, radius);
    }

    @Override
    public boolean insertObject(O agent) {
        return delegate().insertObject(agent);
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public boolean removeIf(Predicate<O> predicate) {
        return delegate().removeIf(predicate);
    }

    @Override
    public Iterable<O> getVisibleNeighbours(O object, double radius) {
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
}
