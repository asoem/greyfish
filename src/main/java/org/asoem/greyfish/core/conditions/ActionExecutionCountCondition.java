package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ActionExecutionCountCondition extends IntCompareCondition {

	@Element(name="action")
	private GFAction action;

    public ActionExecutionCountCondition(ActionExecutionCountCondition condition, DeepCloner map) {
        super(condition, map);
        this.action = map.cloneField(condition.action, GFAction.class);
    }

    @Override
	protected Integer getCompareValue(ParallelizedSimulation simulation) {
		return action.getExecutionCount();
	}

	@Override
	public void configure(ConfigurationHandler e) {
		super.configure(e);

		e.add(new FiniteSetValueAdaptor<GFAction>("", GFAction.class) {
            @Override
            protected void set(GFAction arg0) {
                action = checkNotNull(arg0);
            }

            @Override
            public GFAction get() {
                return action;
            }

            @Override
            public Iterable<GFAction> values() {
                return agent.get().getActions();
            }
        });
	}

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new ActionExecutionCountCondition(this, cloner);
    }

    private ActionExecutionCountCondition() {
        this(new Builder());
    }

    protected ActionExecutionCountCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.action = builder.action;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ActionExecutionCountCondition> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public ActionExecutionCountCondition build() { return new ActionExecutionCountCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends IntCompareCondition.AbstractBuilder<T> {
        private GFAction action;

        public T executionCountOf(GFAction action) { this.action = checkNotNull(action); return self(); }
    }
}
