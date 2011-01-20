package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

public class LastExecutedActionCondition extends LeafCondition {

    @Element(name="actions", required=false)
    private GFAction parameterAction;

    @Override
    public boolean evaluate(Simulation simulation) {
        return isSameAction(parameterAction, getComponentOwner().getLastExecutedAction());
    }

    private static boolean isSameAction(GFAction a1, GFAction a2) {
        return Objects.equal(a1, a2);
//        return Equivalences.equals().equivalent(a1, a2);  // TODO: Make comparison more strict if we can guaranty parameterAction to be not null
    }

    @Override
    public void export(Exporter e) {
        e.addField( new ValueSelectionAdaptor<GFAction>("Action", GFAction.class, parameterAction, componentOwner.getActions()) {
            @Override
            protected void writeThrough(GFAction arg0) {
                parameterAction = arg0;
            }
        });
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected LastExecutedActionCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.parameterAction = builder.parameterAction;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFAction parameterAction;

        public T action(GFAction action) { this.parameterAction = action; return self(); }

        protected T fromClone(LastExecutedActionCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    action(deepClone(component.parameterAction, mapDict));
            return self();
        }

        public LastExecutedActionCondition build() { return new LastExecutedActionCondition(this); }
    }
}
