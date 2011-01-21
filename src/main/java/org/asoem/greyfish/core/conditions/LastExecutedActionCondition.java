package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class LastExecutedActionCondition extends LeafCondition {

    @Element(name="actions", required=false)
    private GFAction action;

    @Override
    public boolean evaluate(Simulation simulation) {
        return isSameAction(action, getComponentOwner().getLastExecutedAction());
    }

    private static boolean isSameAction(GFAction a1, GFAction a2) {
        return Objects.equal(a1, a2);
//        return Equivalences.equals().equivalent(a1, a2);  // TODO: Make comparison more strict if we can guaranty theAction to be not null
    }

    @Override
    public void export(Exporter e) {
        e.addField( new ValueSelectionAdaptor<GFAction>("Action", GFAction.class, action, componentOwner.getActions()) {
            @Override
            protected void writeThrough(GFAction arg0) {
                action = checkFrozen(checkNotNull(arg0));
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
        this.action = builder.action;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<LastExecutedActionCondition> {
        private Builder() {};
        @Override protected Builder self() { return this; }
        @Override public LastExecutedActionCondition build() { return new LastExecutedActionCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFAction action;

        public T theLastExecutedActionWas(GFAction action) { this.action = action; return self(); }

        protected T fromClone(LastExecutedActionCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    theLastExecutedActionWas(deepClone(component.action, mapDict));
            return self();
        }
    }
}
