package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Object2D;
import org.simpleframework.xml.Element;

public class Placeholder extends GFAgentDecorator {


    @Element(name="prototype")
    @Override
    protected IndividualInterface getDelegate() {
        return super.getDelegate();
    }

    @Element(name = "point")
    private final Location2D location;

	public Placeholder(@Element(name = "prototype") IndividualInterface deepClonable,
                       @Element(name = "point") Location2D location2d) {
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


}
