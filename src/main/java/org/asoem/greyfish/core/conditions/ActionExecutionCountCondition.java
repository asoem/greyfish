package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionExecutionCountCondition extends IntCompareCondition {

	@Element(name="actions")
	private GFAction parameterAction;
	
	@Override
	protected Integer getCompareValue(Simulation simulation) {
		return parameterAction.getExecutionCount();
	}

	@Override
	public void export(Exporter e) {
		super.export(e);
		
		final List<GFAction> list = (componentOwner != null) ? getComponentOwner().getActions() : new ArrayList<GFAction>();
		e.addField( new ValueSelectionAdaptor<GFAction>("", GFAction.class, parameterAction, list) {

			@Override
			protected void writeThrough(GFAction arg0) {
				ActionExecutionCountCondition.this.parameterAction = arg0;
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
        this.parameterAction = builder.parameterAction;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends IntCompareCondition.AbstractBuilder<T> {
        private GFAction parameterAction;

        public T action(GFAction action) { this.parameterAction = action; return self(); }

        protected T fromClone(ActionExecutionCountCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    action(deepClone(component.parameterAction, mapDict));
            return self();
        }

        public ActionExecutionCountCondition build() { return new ActionExecutionCountCondition(this); }
    }
}
