package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.ActionContext;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags="condition")
public class LastExecutionTimeCondition extends LeafCondition {

	@Element(name="action")
	private GFAction action;
	
	@Element(name="steps")
	private int steps;

    protected LastExecutionTimeCondition(LastExecutionTimeCondition condition, CloneMap map) {
        super(condition, map);
        this.action = map.clone(condition.action, GFAction.class);
        this.steps = condition.steps;
    }

    @Override
	public boolean evaluate(ActionContext context) {
		return action != null
			&& action.wasNotExecutedForAtLeast(context.getSimulation(), steps);
	}

    @Override
    public LastExecutionTimeCondition deepCloneHelper(CloneMap map) {
        return new LastExecutionTimeCondition(this, map);
    }

    @Override
	public void configure(ConfigurationHandler e) {
		super.configure(e);
		
		e.add(new ValueAdaptor<Integer>("Steps", Integer.class) {

            @Override
            protected void set(Integer arg0) {
                steps = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public Integer get() {
                return steps;
            }
        });
		
		e.add(new FiniteSetValueAdaptor<GFAction>("Action", GFAction.class) {

            @Override
            protected void set(GFAction arg0) {
                action = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public GFAction get() {
                return action;
            }

            @Override
            public Iterable<GFAction> values() {
                return Iterables.filter(getComponentOwner().getActions(), GFAction.class);
            }
        });
	}

    @SimpleXMLConstructor
    private LastExecutionTimeCondition() {
        this(new Builder());
    }

    protected LastExecutionTimeCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.action = builder.action;
        this.steps = builder.steps;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<LastExecutionTimeCondition> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public LastExecutionTimeCondition build() { return new LastExecutionTimeCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFAction action;
        private int steps;

        public T theAction(GFAction action) { this.action = checkNotNull(action); return self(); }
        public T wasNotExecutedFor(int steps) { this.steps = steps; return self(); }
    }
}
