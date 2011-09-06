package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.ActionContext;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 * Pre-Evaluate an other actions
 */
public class ActionWillExecuteCondition extends LeafCondition {

    private final GFAction parameterAction;

    public ActionWillExecuteCondition(ActionWillExecuteCondition condition, CloneMap map) {
        super(condition, map);
        this.parameterAction = map.clone(condition.parameterAction, GFAction.class);
    }

    @Override
    public boolean evaluate(ActionContext context) {
        return parameterAction.evaluateConditions(context);
    }

    @Override
    public ActionWillExecuteCondition deepCloneHelper(CloneMap map) {
        return new ActionWillExecuteCondition(this, map);
    }

    protected ActionWillExecuteCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.parameterAction = builder.parameterAction;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ActionWillExecuteCondition> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public ActionWillExecuteCondition build() { return new ActionWillExecuteCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFAction parameterAction;

        public T followingActionWillExecute(GFAction action) { this.parameterAction = checkNotNull(action); return self(); }
    }
}
