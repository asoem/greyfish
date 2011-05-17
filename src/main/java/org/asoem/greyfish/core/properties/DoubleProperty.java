package org.asoem.greyfish.core.properties;

import com.google.common.collect.Ordering;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.primitives.Doubles.asList;

@ClassGroup(tags="property")
public class DoubleProperty extends AbstractWellOrderedSetElementProperty<Double> implements ContinuousProperty<Double> {

    @SimpleXMLConstructor
    private DoubleProperty() {
        this(new Builder());
    }

    protected DoubleProperty(DoubleProperty property, CloneMap cloneMap) {
        super(property, cloneMap);
    }

    @Override
    public DoubleProperty deepCloneHelper(CloneMap cloneMap) {
        return new DoubleProperty(this, cloneMap);
    }

    @Override
	public void export(Exporter e) {
		super.export(e, Double.class);
	}

	public void subtract(double val) {
		setValue(value - val);
	}
	
	public void add(Double val) {
		setValue(value + val);
	}

    protected DoubleProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
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

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<DoubleProperty> {
        private Builder() {lowerBound(0.0).upperBound(0.0).initialValue(0.0);}
        @Override protected Builder self() { return this; }
        @Override public DoubleProperty build() {
            checkState(lowerBound != null);
            checkState(upperBound != null);
            checkState(initialValue != null);
            checkState(Ordering.<Comparable>natural().isOrdered(asList(lowerBound, initialValue, upperBound)));
            return new DoubleProperty(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractWellOrderedSetElementProperty.AbstractBuilder<T, Double> {
    }
}
