package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

@Tagged("conditions")
public class LastExecutionTimeCondition extends LeafCondition {

	@Element(name="action")
	private AgentAction action;
	
	@Element(name="steps")
	private int steps;

    protected LastExecutionTimeCondition(LastExecutionTimeCondition condition, DeepCloner map) {
        super(condition, map);
        this.action = map.getClone(condition.action, AgentAction.class);
        this.steps = condition.steps;
    }

    @Override
	public boolean apply(AgentAction action) {
		return this.action != null
			&& this.action.wasNotExecutedForAtLeast(steps);
	}

    @Override
    public LastExecutionTimeCondition deepClone(DeepCloner cloner) {
        return new LastExecutionTimeCondition(this, cloner);
    }

    @Override
	public void configure(ConfigurationHandler e) {
		super.configure(e);
		
		e.add("Steps", new AbstractTypedValueModel<Integer>() {

            @Override
            protected void set(Integer arg0) {
                steps = checkNotNull(arg0);
            }

            @Override
            public Integer get() {
                return steps;
            }
        });
		
		e.add("Action", new SetAdaptor<AgentAction>(AgentAction.class) {

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
                return Iterables.filter(agent().getProperties(), AgentAction.class);
            }
        });
	}

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private LastExecutionTimeCondition() {
        this(new Builder());
    }

    protected LastExecutionTimeCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.action = builder.action;
        this.steps = builder.steps;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<LastExecutionTimeCondition, Builder> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override protected LastExecutionTimeCondition checkedBuild() { return new LastExecutionTimeCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends LastExecutionTimeCondition, T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {
        private AgentAction action;
        private int steps;

        public T theAction(AgentAction action) { this.action = checkNotNull(action); return self(); }
        public T wasNotExecutedFor(int steps) { this.steps = steps; return self(); }
    }
}
