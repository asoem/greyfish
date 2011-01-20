package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

/**
 * @author christoph
 * Pre-Evaluate an other actions
 */
public class ActionWillExecuteCondition extends LeafCondition {

    private GFAction parameterAction;

    @Override
    public boolean evaluate(Simulation simulation) {
        return parameterAction.evaluate(simulation);
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected ActionWillExecuteCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.parameterAction = builder.parameterAction;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFAction parameterAction;

        public T action(GFAction action) { this.parameterAction = action; return self(); }

        protected T fromClone(ActionWillExecuteCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    action(deepClone(component.parameterAction, mapDict));
            return self();
        }

        public ActionWillExecuteCondition build() { return new ActionWillExecuteCondition(this); }
    }
}
