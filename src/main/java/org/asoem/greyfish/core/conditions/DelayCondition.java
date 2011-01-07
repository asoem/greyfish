package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.simpleframework.xml.Element;

public class DelayCondition extends LeafCondition {

	private static final long serialVersionUID = -3584101176616818104L;

	private static final transient Boolean[] BOOLEANS = new Boolean[] {Boolean.TRUE, Boolean.FALSE};

	protected transient int counter;

	@Element(name="delay")
	protected int parameterDelay;

	@Element(name="trailer")
	protected Boolean parameterTrailingConstant = Boolean.TRUE;

	public DelayCondition() {
	}

	protected DelayCondition(DelayCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		this.parameterDelay = condition.parameterDelay;
		this.parameterTrailingConstant = condition.parameterTrailingConstant;
	}

	@Override
	public boolean evaluate(Simulation simulation) {
		if (counter > 0) {
			--counter;
			return false;
		}
		else if (counter == 0) {
			--counter;
			return true;
		}
		else
			return parameterTrailingConstant.booleanValue();
	}

	public void reset() {
		this.counter = parameterDelay;
	}
	
	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		reset();
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new DelayCondition(this, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		// TODO: implement
	}
}
