package org.asoem.sico.core.space;

import org.asoem.sico.utils.DeepClonable;
import org.simpleframework.xml.Element;

public class Placeholder extends Object2D {

	@Element(name="prototype")
	private DeepClonable deepClonable;
	
	public Placeholder(@Element(name="prototype") DeepClonable deepClonable) {
		super(new Location2D());
		this.deepClonable = deepClonable;
	}
	
	public Placeholder(@Element(name="point") Location2D location2d) {
		super(location2d);
		this.deepClonable = null;
	}
	
	public Placeholder(@Element(name="point") Location2D location2d,
			@Element(name="prototype") DeepClonable deepClonable) {
		super(location2d);
		this.deepClonable = deepClonable;
	}

	public DeepClonable getPrototype() {
		return deepClonable;
	}
}
