package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActionExecutionCountCondition extends IntCompareCondition {

	@Element(name="action")
	private GFAction action;

    public ActionExecutionCountCondition(ActionExecutionCountCondition condition, DeepCloner map) {
        super(condition, map);
        this.action = map.cloneField(condition.action, GFAction.class);
    }

    @Override
	protected Integer getCompareValue(Simulation simulation) {
		return action.getExecutionCount();
	}

	@Override
	public void configure(ConfigurationHandler e) {
		super.configure(e);

		e.add("", new SetAdaptor<GFAction>("", GFAction.class) {
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
                return agent().getActions();
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

    protected ActionExecutionCountCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.action = builder.action;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<ActionExecutionCountCondition,Builder> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public ActionExecutionCountCondition checkedBuild() { return new ActionExecutionCountCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends ActionExecutionCountCondition, T extends AbstractBuilder<E, T>> extends IntCompareCondition.AbstractBuilder<E,T> {
        private GFAction action;

        public T executionCountOf(GFAction action) { this.action = checkNotNull(action); return self(); }
    }
}
