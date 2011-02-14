package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class IntCompareCondition extends CompareCondition<Integer> {

    protected IntCompareCondition(IntCompareCondition condition, CloneMap map) {
        super(condition, map);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.add(new ValueAdaptor<Integer>("Value", Integer.class) {

            @Override
            protected void set(Integer arg0) {
                value = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public Integer get() {
                return value;
            }
        });
    }

    protected IntCompareCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends CompareCondition.AbstractBuilder<T, Integer> {}
}
