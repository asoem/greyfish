package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Object2D;
import org.simpleframework.xml.Element;

public class Placeholder extends GFAgentDecorator {

    @Element(name = "placeholderObject")
    private final Object2D placeholderObject;

    public Placeholder(@Element(name = "delegate") IndividualInterface deepClonable,
                       @Element(name = "placeholderObject") Object2D placeholderObject) {
        super(deepClonable);
        this.placeholderObject = placeholderObject;
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException();
    }

    public static Placeholder newInstance(Prototype prototype, Object2D location2D) {
        return new Placeholder(prototype, location2D);
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
    public Location2D getAnchorPoint() {
        return placeholderObject;
    }

    @Override
    public double getOrientation() {
        return placeholderObject.getOrientation();
    }

    @Override
    public void setOrientation(double alpha) {
        placeholderObject.setOrientation(alpha);
    }

    public Prototype getPrototype() {
        return Prototype.class.cast(getDelegate());
    }
}
