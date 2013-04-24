package org.asoem.greyfish.core.properties;

import com.google.common.collect.Ordering;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.primitives.Doubles.asList;

@Tagged("properties")
public class DoubleProperty<A extends Agent<A, ?>> extends AbstractRangeElementProperty<Double, A> implements AgentProperty<A,Double> {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public DoubleProperty() {
        this(new Builder<A>());
    }

    protected DoubleProperty(AbstractBuilder<A, ? extends DoubleProperty<A>, ? extends AbstractBuilder<A,? extends DoubleProperty<A>,?>> builder) {
        super(builder);
    }

    protected DoubleProperty(DoubleProperty<A> property, DeepCloner cloner) {
        super(property, cloner);
    }

    @Override
    public DoubleProperty<A> deepClone(DeepCloner cloner) {
        return new DoubleProperty<A>(this, cloner);
    }

    public void subtract(double val) {
        checkAndSet(value - val);
	}
	
	public void add(Double val) {
		checkAndSet(value + val);
	}

    @Override
    public void set(Double value) {
        this.value = value;
    }

    @Override
    public Double get() {
        return value;
    }

    public static <A extends Agent<A, ?>> Builder<A> with() { return new Builder<A>(); }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, DoubleProperty<A>, Builder<A>> {
        public Builder() {lowerBound(0.0).upperBound(0.0).initialValue(0.0);}
        @Override protected Builder<A> self() { return this; }
        @Override public DoubleProperty<A> checkedBuild() {
            checkState(lowerBound != null);
            checkState(upperBound != null);
            checkState(initialValue != null);
            checkState(Ordering.<Comparable<Double>>natural().isOrdered(asList(lowerBound, initialValue, upperBound)));
            return new DoubleProperty<A>(this); }
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, E extends DoubleProperty<A>, T extends AbstractBuilder<A, E, T>> extends AbstractRangeElementProperty.AbstractBuilder<A, E, T, Double> {   }
}
