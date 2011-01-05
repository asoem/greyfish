package org.asoem.sico.core.conditions;

import java.util.Map;

import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public abstract class CompareCondition<T extends Comparable<T>> extends LeafCondition {

	private static final long serialVersionUID = 3074417055207204804L;

	@Attribute(name="comparator")
	protected Comparator parameterComparator = Comparator.EQ;
	
	@Element(name="value")
	protected T value;

	public CompareCondition() {
	}
	
	public CompareCondition(
			CompareCondition<T> condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		this.parameterComparator = condition.parameterComparator;
		this.value = condition.value;
	}
	
	@Override
	public boolean evaluate(Simulation simulation) {
		return parameterComparator.compare(getCompareValue(simulation), value);
	}
	
	protected abstract T getCompareValue(Simulation simulation);
	
	@Override
	public void export(Exporter e) {
		e.addField( new ValueSelectionAdaptor<Comparator>("", Comparator.class, parameterComparator, Comparator.values()) {
			@Override
			protected void writeThrough(Comparator arg0) {
				CompareCondition.this.parameterComparator = arg0;
			}
		});
	}
}
