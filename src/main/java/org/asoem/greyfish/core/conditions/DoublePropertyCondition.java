package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

@Tagged("conditions")
public class DoublePropertyCondition extends DoubleCompareCondition {

	@Element(name="property")
	private DoubleProperty<A> doubleProperty;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public DoublePropertyCondition() {
        this(new Builder());
    }

    @Override
    public DoublePropertyCondition deepClone(DeepCloner cloner) {
        return new DoublePropertyCondition(this, cloner);
    }

    private DoublePropertyCondition(AbstractBuilder<?, ?> builder) {
        super(builder);
        this.doubleProperty = builder.doubleProperty;
    }

    private DoublePropertyCondition(DoublePropertyCondition condition, DeepCloner map) {
        super(condition, map);
        this.doubleProperty = map.getClone(condition.doubleProperty, DoubleProperty<A>.class);
    }

    @Override
	public void configure(ConfigurationHandler e) {
		super.configure(e);
		e.add("", new SetAdaptor<DoubleProperty<A>>(DoubleProperty<A>.class
        ) {

            @Override
            protected void set(DoubleProperty<A> arg0) {
                doubleProperty = arg0;
            }

            @Override
            public DoubleProperty<A> get() {
                return doubleProperty;
            }

            @Override
            public Iterable<DoubleProperty<A>> values() {
                return Iterables.filter(agent().getProperties(), DoubleProperty<A>.class);
            }
        });
	}

	@Override
	protected Double getCompareValue() {
		return doubleProperty.getValue();
	}

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<DoublePropertyCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override public DoublePropertyCondition checkedBuild() { return new DoublePropertyCondition(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends DoublePropertyCondition, T extends AbstractBuilder<E,T>> extends DoubleCompareCondition.AbstractBuilder<E,T> {
        private DoubleProperty<A> doubleProperty;

        public T valueOf(DoubleProperty<A> doubleProperty) { this.doubleProperty = doubleProperty; return self(); }
    }
}
