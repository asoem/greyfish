package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;

import java.util.Map;

public abstract class DoubleCompareCondition extends CompareCondition<Double> {
	
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

    protected DoubleCompareCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends CompareCondition.AbstractBuilder<T, Double> {
        protected T fromClone(IntCompareCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }
    }
}
