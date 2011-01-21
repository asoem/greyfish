package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags="condition")
public class LastExecutionTimeCondition extends LeafCondition {

	@Element(name="actions")
	private GFAction action;
	
	@Element(name="steps")
	private int steps;

	@Override
	public boolean evaluate(Simulation simulation) {
		return action != null
			&& action.wasNotExecutedForAtLeast(simulation, steps);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Builder().fromClone(this, mapDict).build();
	}

	@Override
	public void export(Exporter e) {
		super.export(e);
		
		e.addField(new ValueAdaptor<Integer>("Steps", Integer.class, steps) {

			@Override
			protected void writeThrough(Integer arg0) {
				steps = checkFrozen(checkNotNull(arg0));
			}
		});
		
		e.addField(new ValueSelectionAdaptor<GFAction>("Action", GFAction.class, action, getComponentOwner().getActions()) {

			@Override
			protected void writeThrough(GFAction arg0) {
				action = checkFrozen(checkNotNull(arg0));
			}
		});
	}

    protected LastExecutionTimeCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.action = builder.action;
        this.steps = builder.steps;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<LastExecutionTimeCondition> {
        private Builder() {};
        @Override protected Builder self() { return this; }
        @Override public LastExecutionTimeCondition build() { return new LastExecutionTimeCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFAction action;
        private int steps;

        public T theAction(GFAction action) { this.action = checkNotNull(action); return self(); }
        public T wasNotExecutedFor(int steps) { this.steps = steps; return self(); }

        protected T fromClone(LastExecutionTimeCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    theAction(deepClone(component.action, mapDict)).
                    wasNotExecutedFor(component.steps);
            return self();
        }
    }
}
