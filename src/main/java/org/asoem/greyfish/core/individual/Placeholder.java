package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Element;


/**
 * An unmodifiable view of an agent at a specified location
 *
 * TODO: it is not enforced yet that the agent is unmodifiable!
 */
public class Placeholder extends ForwardingAgent implements Object2D {

    @Element(name = "placeholderObject")
    private final Object2D placeholderObject;

    @Element(name = "delegate")
    private final Agent delegate;

    public Placeholder(@Element(name = "delegate") Agent prototype,
                       @Element(name = "placeholderObject") Object2D placeholderObject) {
        this.delegate = prototype;
        this.placeholderObject = placeholderObject; // TODO: store an immutable copy of placeholderObject
    }

    public static Placeholder newInstance(Agent prototype, Object2D location2D) {
        return new Placeholder(prototype, location2D);
    }

    @Override
    protected Agent delegate() {
        return delegate;
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getOrientationAngle() {
        return placeholderObject.getOrientationAngle();
    }

    @Override
    public double[] getBoundingVolume() {
        return placeholderObject.getBoundingVolume();
    }

    @Override
    public Motion2D getMotion() {
        return delegate.getMotion();
    }

    @Override
    public void setRotation(double alpha) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getX() {
        return placeholderObject.getX();
    }

    @Override
    public double getY() {
        return placeholderObject.getY();
    }

    @Override
    public double[] getCoordinates() {
        return placeholderObject.getCoordinates();
    }

    @Override
    public int getDimensions() {
        return placeholderObject.getDimensions();
    }

    @Override
    public double[] getOrientation() {
        return placeholderObject.getOrientation();
    }
}
