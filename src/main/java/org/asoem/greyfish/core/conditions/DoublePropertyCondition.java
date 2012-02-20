package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

@ClassGroup(tags = "conditions")
public class DoublePropertyCondition extends DoubleCompareCondition {

	@Element(name="property")
	private DoubleProperty doubleProperty;

    @SimpleXMLConstructor
    public DoublePropertyCondition() {
        this(new Builder());
    }

    @Override
    public DoublePropertyCondition deepClone(DeepCloner cloner) {
        return new DoublePropertyCondition(this, cloner);
    }

    protected DoublePropertyCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.doubleProperty = builder.doubleProperty;
    }

    protected DoublePropertyCondition(DoublePropertyCondition condition, DeepCloner map) {
        super(condition, map);
        this.doubleProperty = map.cloneField(condition.doubleProperty, DoubleProperty.class);
    }

    @Override
	public void configure(ConfigurationHandler e) {
		super.configure(e);
		e.add("", new SetAdaptor<DoubleProperty>(DoubleProperty.class
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
                return Iterables.filter(agent().getProperties(), DoubleProperty.class);
            }
        });
	}

	@Override
	protected Double getCompareValue(Simulation simulation) {
		return doubleProperty.get();
	}

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<DoublePropertyCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override public DoublePropertyCondition checkedBuild() { return new DoublePropertyCondition(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends DoublePropertyCondition, T extends AbstractBuilder<E,T>> extends DoubleCompareCondition.AbstractBuilder<E,T> {
        private DoubleProperty doubleProperty;

        public T valueOf(DoubleProperty doubleProperty) { this.doubleProperty = doubleProperty; return self(); }
    }
}
