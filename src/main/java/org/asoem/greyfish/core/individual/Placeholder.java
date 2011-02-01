package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.simpleframework.xml.Element;

public class Placeholder extends GFAgentDecorator {

    @Element(name="point")
    private final Location2DInterface location;

    private Placeholder(@Element(name="prototype") IndividualInterface delegate,
                        @Element(name="point") Location2DInterface location) {
        super(delegate);
        this.location = location;
    }

    protected Placeholder(Placeholder delegate, CloneMap map) {
        super(delegate.getDelegate(), map);
        this.location = delegate.location;
    }

    public static Placeholder newInstance(IndividualInterface delegate, Location2DInterface location) {
        return new Placeholder(delegate, location);
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new Placeholder(this, map);
    }

    @Element(name="prototype")
    @Override
    public IndividualInterface getDelegate() {
        return super.getDelegate();
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

    @Override
    public void setAnchorPoint(Location2DInterface location2d) {
        throw new UnsupportedOperationException();
    }

    public Prototype asPrototype() {
        return Prototype.newInstance(getDelegate());
    }

    public Agent asAgent() {
        Agent ret = Agent.newInstance(getDelegate());
        ret.setAnchorPoint(location);
        return ret;
    }
}
