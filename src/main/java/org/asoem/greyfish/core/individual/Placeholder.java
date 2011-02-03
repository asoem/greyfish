package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.space.Location2DInterface;
import org.simpleframework.xml.Element;

public class Placeholder extends GFAgentDecorator {

    @Element(name = "location")
    private final Location2DInterface location;

    public Placeholder(@Element(name = "delegate") IndividualInterface deepClonable,
                       @Element(name = "location") Location2DInterface location2d) {
        super(deepClonable);
        this.location = location2d;
    }

    @Override
    public void setId(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException();
    }

    public static Placeholder newInstance(IndividualInterface prototype, Location2DInterface location2D) {
        return new Placeholder(prototype, location2D);
    }

    @Override
    public double getX() {
        return location.getX();
    }

    @Override
    public double getY() {
        return location.getY();
    }

    @Override
    public Location2DInterface getAnchorPoint() {
        return location;
    }
}
