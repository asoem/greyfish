package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActionExecutionCountCondition extends IntCompareCondition {

	@Element(name="actions")
	private GFAction action;
	
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
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected ActionExecutionCountCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.action = builder.action;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ActionExecutionCountCondition> {
        private Builder() {};
        @Override protected Builder self() { return this; }
        @Override public ActionExecutionCountCondition build() { return new ActionExecutionCountCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends IntCompareCondition.AbstractBuilder<T> {
        private GFAction action;

        public T executionCountOf(GFAction action) { this.action = checkNotNull(action); return self(); }

        protected T fromClone(ActionExecutionCountCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    executionCountOf(deepClone(component.action, mapDict));
            return self();
        }
    }
}
