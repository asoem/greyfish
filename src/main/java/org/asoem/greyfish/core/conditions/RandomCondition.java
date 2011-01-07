package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

public class RandomCondition extends LeafCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8799634725139546159L;

	@Element(name="propability")
	private double parameterTruePropability;

	public RandomCondition() {
		this.parameterTruePropability = 0.5;
	}

	public RandomCondition(RandomCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		this.parameterTruePropability = condition.parameterTruePropability;
	}

	@Override
	public boolean evaluate(Simulation simulation) {
		return Math.random() < parameterTruePropability;
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new RandomCondition(this, mapDict);
	}

	@Override
	public void export(Exporter e) {
		e.addField( new ValueAdaptor<Double>("", Double.class, parameterTruePropability) {
			@Override
			protected void writeThrough(Double arg0) {
				RandomCondition.this.parameterTruePropability = arg0;
			}
		});
	}
}
