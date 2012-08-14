package org.asoem.greyfish.core.properties;

import com.google.common.collect.Ordering;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.primitives.Doubles.asList;

@ClassGroup(tags="properties")
public class DoubleProperty extends AbstractRangeElementProperty<Double> implements MutableProperty<Double> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(DoubleProperty.class);

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public DoubleProperty() {
        this(new Builder());
    }

    protected DoubleProperty(AbstractBuilder<?,?> builder) {
        super(builder);
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
        // TODO: handle bounds violation
        setValue(value - val);
        LOGGER.debug("Subtracting: {}, Remaining: {}", val, value);
	}
	
	public void add(Double val) {
		setValue(value + val);
	}

    @Override
    public void set(Double amount) {
        value = amount;
    }

    @Override
    public Double getValue() {
        return value;
    }

    public static Builder with() { return new Builder(); }

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

    protected static abstract class AbstractBuilder<E extends DoubleProperty, T extends AbstractBuilder<E, T>> extends AbstractRangeElementProperty.AbstractBuilder<E, T, Double> {
    }
}
