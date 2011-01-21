package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class IntCompareCondition extends CompareCondition<Integer> {

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField( new ValueAdaptor<Integer>("Value", Integer.class, value) {

            @Override
            protected void writeThrough(Integer arg0) {
                value = checkFrozen(checkNotNull(arg0));
            }
        });
    }

    protected IntCompareCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends CompareCondition.AbstractBuilder<T, Integer> {
        protected T fromClone(IntCompareCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }
    }
}
