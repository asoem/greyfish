package org.asoem.sico.core.conditions;

import java.util.Map;

import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueAdaptor;

public abstract class DoubleCompareCondition extends CompareCondition<Double> {

	private static final long serialVersionUID = -7349763764527840921L;
	
	public DoubleCompareCondition() {
	}
	
	protected DoubleCompareCondition(
			DoubleCompareCondition actionExecutionCountCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(actionExecutionCountCondition, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField( new ValueAdaptor<Double>("", Double.class, value) {

			@Override
			protected void writeThrough(Double arg0) {
				DoubleCompareCondition.this.value = arg0;
			}
		});
	}
}
