package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.simpleframework.xml.Element;

public class Placeholder extends GFAgentDecorator {


    @Element(name="prototype")
    @Override
    protected IndividualInterface getDelegate() {
        return super.getDelegate();
    }

    @Element(name = "point")
    private final Location2DInterface location;

    public Placeholder(@Element(name = "prototype") IndividualInterface deepClonable,
                       @Element(name = "point") Location2DInterface location2d) {
        super(deepClonable);
        this.location = location2d;
    }

    public Prototype asPrototype() {
        return Prototype.newInstance(getDelegate());
    }

    public Agent asAgent() {
        return Agent.newInstance(getDelegate());
    }

    /**
     *
     * @return A deepClone of this placeholders simulationObject with anchorpoint set to this Placeholders anchorpoint
     */
    public Agent createReplacement() {
        Agent ret = Agent.newInstance(getDelegate().deepClone(IndividualInterface.class));
        ret.setAnchorPoint(location);
        return ret;
    }


    @Override
    public void setId(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Simulation simulation) {
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
