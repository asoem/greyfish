package org.asoem.greyfish.utils.space;

import com.google.common.collect.ForwardingObject;


public abstract class ForwardingObject2D extends ForwardingObject implements Object2D {

    @Override
    protected abstract Object2D delegate();

    @Override
    public int getDimension() {
        return delegate().getDimension();
    }

    @Override
    public Point2D getCentroid() {
        return delegate().getCentroid();
    }
}
