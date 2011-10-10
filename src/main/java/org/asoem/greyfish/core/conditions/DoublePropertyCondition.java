package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.simpleframework.xml.Element;

public class DoublePropertyCondition extends DoubleCompareCondition {

	@Element(name="property")
	private DoubleProperty doubleProperty;

    protected DoublePropertyCondition(DoublePropertyCondition condition, DeepCloner map) {
        super(condition, map);
        this.doubleProperty = map.cloneField(condition.doubleProperty, DoubleProperty.class);
    }

    @Override
	public void configure(ConfigurationHandler e) {
		super.configure(e);
		e.add(new FiniteSetValueAdaptor<DoubleProperty>("", DoubleProperty.class
        ) {

            @Override
            protected void set(DoubleProperty arg0) {
                doubleProperty = arg0;
            }

            @Override
            public DoubleProperty get() {
                return doubleProperty;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(agent.get().getProperties(), DoubleProperty.class);
            }
        });
	}

	@Override
	protected Double getCompareValue(Simulation simulation) {
		return doubleProperty.get();
	}

    @Override
    public DoublePropertyCondition deepClone(DeepCloner cloner) {
        return new DoublePropertyCondition(this, cloner);
    }

    private DoublePropertyCondition() {
        this(new Builder());
    }

    protected DoublePropertyCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.doubleProperty = builder.doubleProperty;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<DoublePropertyCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override public DoublePropertyCondition checkedBuild() { return new DoublePropertyCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends DoublePropertyCondition, T extends AbstractBuilder<E,T>> extends DoubleCompareCondition.AbstractBuilder<E,T> {
        private DoubleProperty doubleProperty;

        public T valueOf(DoubleProperty doubleProperty) { this.doubleProperty = doubleProperty; return self(); }
    }
}
