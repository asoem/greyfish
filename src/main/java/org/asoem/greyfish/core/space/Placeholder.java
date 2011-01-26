package org.asoem.greyfish.core.space;

import org.asoem.greyfish.core.individual.SimulationObject;
import org.simpleframework.xml.Element;

public class Placeholder extends Object2D {

	@Element(name="prototype")
	private final SimulationObject deepClonable;

	public Placeholder(@Element(name="prototype") SimulationObject deepClonable) {
		super(new Location2D());
		this.deepClonable = deepClonable;
	}

	public Placeholder(@Element(name="point") Location2D location2d) {
		super(location2d);
		this.deepClonable = null;
	}

	public Placeholder(@Element(name = "prototype") SimulationObject deepClonable,
                       @Element(name = "point") Location2D location2d) {
		super(location2d);
		this.deepClonable = deepClonable;
	}

	public SimulationObject getPrototype() {
		return deepClonable;
	}

    /**
     *
     * @return A deepClone of this placeholders simulationObject with anchorpoint set to this Placeholders anchorpoint
     */
    public SimulationObject createReplacement() {
        SimulationObject ret = deepClonable.deepClone();
        ret.setAnchorPoint(anchorPoint);
        return ret;
    }
}
