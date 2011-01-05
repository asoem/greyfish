package org.asoem.sico.core.properties;

import java.util.Map;

import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;

@ClassGroup(tags="property")
public class IntProperty extends OrderedSetProperty<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3487136329379950991L;

	public IntProperty() {
		super(0, 100, 100);
	}

	protected IntProperty(IntProperty intProperty,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(intProperty, mapDict);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new IntProperty(this, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e, Integer.class);
	}

	public void substract(int costs) {
		setValue(value-costs);
	}
}
