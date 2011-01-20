package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.properties.IntProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

public class IntPropertyCondition extends IntCompareCondition {

	@Element(name="property")
	private IntProperty intProperty;
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField(new ValueSelectionAdaptor<IntProperty>("", IntProperty.class, intProperty, getComponentOwner().getProperties(IntProperty.class)) {

			@Override
			protected void writeThrough(IntProperty arg0) {
				intProperty = arg0;
			}
		});
	}

	@Override
	protected Integer getCompareValue(Simulation simulation) {
		return intProperty.getValue();
	}

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected IntPropertyCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.intProperty = builder.intProperty;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends IntCompareCondition.AbstractBuilder<T> {
        private IntProperty intProperty;

        public T intProperty(IntProperty intProperty) { this.intProperty = intProperty; return self(); }

        protected T fromClone(IntPropertyCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    intProperty(deepClone(component.intProperty, mapDict));
            return self();
        }

        public IntPropertyCondition build() { return new IntPropertyCondition(this); }
    }
}
