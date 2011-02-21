package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.simpleframework.xml.Element;

public class DoublePropertyCondition extends DoubleCompareCondition {

	@Element(name="property")
	private DoubleProperty doubleProperty;

    protected DoublePropertyCondition(DoublePropertyCondition condition, CloneMap map) {
        super(condition, map);
        this.doubleProperty = map.clone(condition.doubleProperty, DoubleProperty.class);
    }

    @Override
	public void export(Exporter e) {
		super.export(e);
		e.add(new FiniteSetValueAdaptor<DoubleProperty>("", DoubleProperty.class
        ) {

            @Override
            protected void set(DoubleProperty arg0) {
                doubleProperty = checkFrozen(arg0);
            }

            @Override
            public DoubleProperty get() {
                return doubleProperty;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(getComponentOwner().getProperties(), DoubleProperty.class);
            }
        });
	}

	@Override
	protected Double getCompareValue(Simulation simulation) {
		return doubleProperty.get();
	}

    @Override
    public DoublePropertyCondition deepCloneHelper(CloneMap map) {
        return new DoublePropertyCondition(this, map);
    }

    private DoublePropertyCondition() {
        this(new Builder());
    }

    protected DoublePropertyCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.doubleProperty = builder.doubleProperty;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<DoublePropertyCondition> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public DoublePropertyCondition build() { return new DoublePropertyCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends DoubleCompareCondition.AbstractBuilder<T> {
        private DoubleProperty doubleProperty;

        public T valueOf(DoubleProperty doubleProperty) { this.doubleProperty = doubleProperty; return self(); }
    }
}
