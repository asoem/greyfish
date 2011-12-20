package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;

public abstract class DoubleCompareCondition extends CompareCondition<Double> {

    protected DoubleCompareCondition(DoublePropertyCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
	public void configure(ConfigurationHandler e) {
		super.configure(e);
		e.add("", new AbstractTypedValueModel<Double>() {
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

    protected DoubleCompareCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<E extends DoubleCompareCondition, T extends AbstractBuilder<E, T>> extends CompareCondition.AbstractBuilder<E, T, Double> {}
}
