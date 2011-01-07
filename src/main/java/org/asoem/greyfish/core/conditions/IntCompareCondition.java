package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;

public abstract class IntCompareCondition extends CompareCondition<Integer> {

	protected IntCompareCondition() {
	}
	
	protected IntCompareCondition(
			IntCompareCondition actionExecutionCountCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(actionExecutionCountCondition, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField( new ValueAdaptor<Integer>("Value", Integer.class, value) {

			@Override
			protected void writeThrough(Integer arg0) {
				value = arg0;
			}
		});
	}
}
