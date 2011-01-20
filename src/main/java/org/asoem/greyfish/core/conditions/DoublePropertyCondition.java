package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

public class DoublePropertyCondition extends DoubleCompareCondition {

	@Element(name="property")
	private DoubleProperty doubleProperty;
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField(new ValueSelectionAdaptor<DoubleProperty>("", DoubleProperty.class, doubleProperty, getComponentOwner().getProperties(DoubleProperty.class)) {

			@Override
			protected void writeThrough(DoubleProperty arg0) {
				doubleProperty = arg0;
			}
		});
	}

	@Override
	protected Double getCompareValue(Simulation simulation) {
		return doubleProperty.getValue();
	}

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected DoublePropertyCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.doubleProperty = builder.doubleProperty;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends DoubleCompareCondition.AbstractBuilder<T> {
        private DoubleProperty doubleProperty;

        public T doubleProperty(DoubleProperty doubleProperty) { this.doubleProperty = doubleProperty; return self(); }

        protected T fromClone(DoublePropertyCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    doubleProperty(deepClone(component.doubleProperty, mapDict));
            return self();
        }

        public DoublePropertyCondition build() { return new DoublePropertyCondition(this); }
    }
}
