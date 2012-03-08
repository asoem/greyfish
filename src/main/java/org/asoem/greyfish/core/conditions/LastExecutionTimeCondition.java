package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags="conditions")
public class LastExecutionTimeCondition extends LeafCondition {

	@Element(name="action")
	private GFAction action;
	
	@Element(name="steps")
	private int steps;

    protected LastExecutionTimeCondition(LastExecutionTimeCondition condition, DeepCloner map) {
        super(condition, map);
        this.action = map.cloneField(condition.action, GFAction.class);
        this.steps = condition.steps;
    }

    @Override
	public boolean apply(Simulation simulation) {
		return action != null
			&& action.wasNotExecutedForAtLeast(simulation, steps);
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
		
		e.add("Action", new SetAdaptor<GFAction>(GFAction.class) {

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
                return Iterables.filter(agent().getProperties(), GFAction.class);
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
        @Override public LastExecutionTimeCondition checkedBuild() { return new LastExecutionTimeCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends LastExecutionTimeCondition, T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {
        private GFAction action;
        private int steps;

        public T theAction(GFAction action) { this.action = checkNotNull(action); return self(); }
        public T wasNotExecutedFor(int steps) { this.steps = steps; return self(); }
    }
}
