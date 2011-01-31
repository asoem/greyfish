package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.ArrayList;
import java.util.List;

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
	public void export(Exporter e) {
		super.export(e);
		
		final List<GFAction> list = (componentOwner != null) ? getComponentOwner().getActions() : new ArrayList<GFAction>();
		e.addField( new ValueSelectionAdaptor<GFAction>("", GFAction.class, action, list) {

			@Override
			protected void writeThrough(GFAction arg0) {
				action = checkFrozen(checkNotNull(arg0));
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
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        super.checkConsistency(components);
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
