package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.ValueAdaptor;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class IntCompareCondition extends CompareCondition<Integer> {

    protected IntCompareCondition(IntCompareCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new ValueAdaptor<Integer>("Value", Integer.class) {

            @Override
            protected void set(Integer arg0) {
                value = checkNotNull(arg0);
            }

            @Override
            public Integer get() {
                return value;
            }
        });
    }

    protected IntCompareCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<E extends IntCompareCondition, T extends AbstractBuilder<E,T>> extends CompareCondition.AbstractBuilder<E,T, Integer> {}
}
