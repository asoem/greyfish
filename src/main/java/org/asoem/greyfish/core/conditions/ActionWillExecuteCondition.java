package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 * Pre-Evaluate an other actions
 */
public class ActionWillExecuteCondition extends LeafCondition {

    private final GFAction parameterAction;

    @Override
    public boolean evaluate(Simulation simulation) {
        return parameterAction.evaluate(simulation);
    }

    @Override
    protected AbstractGFComponent deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected ActionWillExecuteCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.parameterAction = builder.parameterAction;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ActionWillExecuteCondition> {
        private Builder() {};
        @Override protected Builder self() { return this; }
        @Override public ActionWillExecuteCondition build() { return new ActionWillExecuteCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFAction parameterAction;

        public T followingActionWillExecute(GFAction action) { this.parameterAction = checkNotNull(action); return self(); }

        protected T fromClone(ActionWillExecuteCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    followingActionWillExecute(deepClone(component.parameterAction, mapDict));
            return self();
        }
    }
}
