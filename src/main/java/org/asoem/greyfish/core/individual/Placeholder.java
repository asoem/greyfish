package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Object2D;
import org.asoem.greyfish.gui.model.Prototype;
import org.simpleframework.xml.Element;

public class Placeholder extends AgentDecorator {

    @Element(name = "placeholderObject")
    private final Object2D placeholderObject;

    public Placeholder(@Element(name = "delegate") Agent prototype,
                       @Element(name = "placeholderObject") Object2D placeholderObject) {
        super(prototype);
        this.placeholderObject = placeholderObject;
    }

    public static Placeholder newInstance(Agent prototype, Object2D location2D) {
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
        return Prototype.class.cast(delegate());
    }
}
