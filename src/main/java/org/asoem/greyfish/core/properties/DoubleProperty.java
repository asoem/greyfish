package org.asoem.greyfish.core.properties;

import com.google.common.collect.Ordering;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloner;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.primitives.Doubles.asList;

@ClassGroup(tags="property")
public class DoubleProperty extends AbstractWellOrderedSetElementProperty<Double> implements ContinuousProperty<Double> {

    @SimpleXMLConstructor
    private DoubleProperty() {
        this(new Builder());
    }

    protected DoubleProperty(DoubleProperty property, DeepCloner cloner) {
        super(property, cloner);
    }

    @Override
    public DoubleProperty deepClone(DeepCloner cloner) {
        return new DoubleProperty(this, cloner);
    }

    @Override
	public void configure(ConfigurationHandler e) {
		super.configure(e, Double.class);
	}

	public void subtract(double val) {
		setValue(value - val);
	}
	
	public void add(Double val) {
		setValue(value + val);
	}

    protected DoubleProperty(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }

    @Override
    public void set(Double amount) {
        value = amount;
    }

    @Override
    public Double get() {
        return value;
    }

    public static final class Builder extends AbstractBuilder<DoubleProperty, Builder> {
        public Builder() {lowerBound(0.0).upperBound(0.0).initialValue(0.0);}
        @Override protected Builder self() { return this; }
        @Override public DoubleProperty checkedBuild() {
            checkState(lowerBound != null);
            checkState(upperBound != null);
            checkState(initialValue != null);
            checkState(Ordering.<Comparable>natural().isOrdered(asList(lowerBound, initialValue, upperBound)));
            return new DoubleProperty(this); }
    }

    protected static abstract class AbstractBuilder<E extends DoubleProperty, T extends AbstractBuilder<E, T>> extends AbstractWellOrderedSetElementProperty.AbstractBuilder<E, T, Double> {
    }
}
