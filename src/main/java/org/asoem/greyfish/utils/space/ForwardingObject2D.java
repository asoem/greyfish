package org.asoem.greyfish.utils.space;

import com.google.common.collect.ForwardingObject;

/**
 * User: christoph
 * Date: 01.10.12
 * Time: 11:50
 */
public abstract class ForwardingObject2D extends ForwardingObject implements Object2D {

    @Override
    protected abstract Object2D delegate();

    @Override
    public double getOrientationAngle() {
        return delegate().getOrientationAngle();
    }

    @Override
    public int getDimensions() {
        return delegate().getDimensions();
    }

    @Override
    public double[] getOrientation() {
        return delegate().getOrientation();
    }

    @Override
    public double[] getBoundingVolume() {
        return delegate().getBoundingVolume();
    }

    @Override
    public Point2D getAnchorPoint() {
        return delegate().getAnchorPoint();
    }
}
