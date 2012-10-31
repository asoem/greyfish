package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 * Pre-Evaluate an other actions
 */
public class ActionWillExecuteCondition extends LeafCondition {

    private final AgentAction parameterAction;

    public ActionWillExecuteCondition(ActionWillExecuteCondition condition, DeepCloner map) {
        super(condition, map);
        this.parameterAction = map.getClone(condition.parameterAction, AgentAction.class);
    }

    @Override
    public boolean evaluate() {
        return parameterAction.evaluateCondition();
    }

    @Override
    public ActionWillExecuteCondition deepClone(DeepCloner cloner) {
        return new ActionWillExecuteCondition(this, cloner);
    }

    protected ActionWillExecuteCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.parameterAction = builder.parameterAction;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<ActionWillExecuteCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override public ActionWillExecuteCondition checkedBuild() { return new ActionWillExecuteCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends ActionWillExecuteCondition, T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {
        private AgentAction parameterAction;

        public T followingActionWillExecute(AgentAction action) { this.parameterAction = checkNotNull(action); return self(); }
    }
}
