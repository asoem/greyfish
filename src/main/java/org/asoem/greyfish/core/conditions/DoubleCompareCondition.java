package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;

public abstract class DoubleCompareCondition extends CompareCondition<Double> {

    protected DoubleCompareCondition(DoublePropertyCondition condition, CloneMap map) {
        super(condition, map);
    }

    @Override
	public void export(Exporter e) {
		super.export(e);
		e.add(new ValueAdaptor<Double>("", Double.class) {
            @Override
            protected void set(Double arg0) {
                value = arg0;
            }

            @Override
            public Double get() {
                return value;
            }
        });
	}

    protected DoubleCompareCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends CompareCondition.AbstractBuilder<T, Double> {}
}
