package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ActionExecutionCountCondition extends IntCompareCondition {

	@Element(name="action")
	private GFAction action;

    public ActionExecutionCountCondition(ActionExecutionCountCondition condition, CloneMap map) {
        super(condition, map);
        this.action = map.clone(condition.action, GFAction.class);
    }

    @Override
	protected Integer getCompareValue(Simulation simulation) {
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
                return Iterables.filter(agent.getActions(), GFAction.class);
            }
        });
	}

    @Override
    public AbstractGFComponent deepCloneHelper(CloneMap map) {
        return new ActionExecutionCountCondition(this, map);
    }

    private ActionExecutionCountCondition() {
        this(new Builder());
    }

    protected ActionExecutionCountCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.action = builder.action;
    }

    @Override
    public void checkConsistency() throws IllegalStateException {
        super.checkConsistency();
        checkState(action != null);
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
