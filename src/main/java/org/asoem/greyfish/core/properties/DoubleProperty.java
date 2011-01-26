package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.lang.Comparables;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

@ClassGroup(tags="property")
public class DoubleProperty extends OrderedSetProperty<Double> {

    private DoubleProperty() {
        this(new Builder());
    }

	@Override
	protected AbstractGFComponent deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Builder().fromClone(this, mapDict).build();
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
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<DoubleProperty> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public DoubleProperty build() {
            checkState(lowerBound != null);
            checkState(upperBound != null);
            checkState(initialValue != null);
            Comparables.areInOrder(lowerBound, initialValue, upperBound);
            return new DoubleProperty(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends OrderedSetProperty.AbstractBuilder<T, Double> {
        protected AbstractBuilder() {
            lowerBound(0.0).upperBound(100.0).initialValue(50.0);
        }

        protected T fromClone(DoubleProperty property, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(property, mapDict);
            return self();
        }
    }
}
