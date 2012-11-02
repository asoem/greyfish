package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActionExecutionCountCondition extends IntCompareCondition {

    @Element(name = "action")
    private AgentAction action;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private ActionExecutionCountCondition() {
        this(new Builder());
    }

    private ActionExecutionCountCondition(AbstractBuilder<?, ?> builder) {
        super(builder);
        this.action = builder.action;
    }

    private ActionExecutionCountCondition(ActionExecutionCountCondition condition, DeepCloner map) {
        super(condition, map);
        this.action = map.getClone(condition.action, AgentAction.class);
    }

    @Override
    protected Integer getCompareValue() {
        return action.getCompletionCount();
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);

        e.add("", new SetAdaptor<AgentAction>(AgentAction.class) {
            @Override
            protected void set(AgentAction arg0) {
                action = checkNotNull(arg0);
            }

            @Override
            public AgentAction get() {
                return action;
            }

            @Override
            public Iterable<AgentAction> values() {
                return agent().getActions();
            }
        });
    }

    @Override
    public ActionExecutionCountCondition deepClone(DeepCloner cloner) {
        return new ActionExecutionCountCondition(this, cloner);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<ActionExecutionCountCondition, Builder> {
        private Builder() {
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected ActionExecutionCountCondition checkedBuild() {
            return new ActionExecutionCountCondition(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends ActionExecutionCountCondition, T extends AbstractBuilder<E, T>> extends IntCompareCondition.AbstractBuilder<E, T> {
        private AgentAction action;

        public T executionCountOf(AgentAction action) {
            this.action = checkNotNull(action);
            return self();
        }
    }
}
