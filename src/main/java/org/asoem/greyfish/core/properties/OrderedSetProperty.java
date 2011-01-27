package org.asoem.greyfish.core.properties;

import com.jgoodies.validation.ValidationResult;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.Comparables;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


public abstract class OrderedSetProperty<E extends Comparable<E>> extends AbstractGFProperty implements DiscreteProperty<E>, OrderedSet<E> {

    @Element(name="min")
    protected E upperBound;

    @Element(name="max")
    protected E lowerBound;

    @Element(name="init")
    protected E initialValue;

    protected E value;

    protected OrderedSetProperty(OrderedSetProperty<E> property, CloneMap cloneMap) {
        super(property, cloneMap);
        this.lowerBound = property.lowerBound;
        this.upperBound = property.upperBound;
        this.initialValue = property.initialValue;
    }

    @Override
    public E getValue() {
        return value;
    }

    public void setValue(E amount) {
        checkNotNull(amount);
        if (Comparables.areInOrder(lowerBound, amount, upperBound)) {
            this.value = amount;
            firePropertyChanged();
        }
        else
        if (GreyfishLogger.isDebugEnabled())
            GreyfishLogger.debug(this.getClass().getSimpleName() + "#setValue("+amount+"): Out of range ("+lowerBound+","+upperBound+")");
    }

    @Override
    public E getUpperBound() {
        return upperBound;
    }

    @Override
    public E getLowerBound() {
        return lowerBound;
    }

    public E getInitialValue() {
        return initialValue;
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        setValue(initialValue);
    }

    public void export(Exporter e, Class<E> clazz) {
        e.addField(new ValueAdaptor<E>("Min", clazz, lowerBound) {

            @Override
            protected void writeThrough(E arg0) {
                lowerBound = checkFrozen(checkNotNull(arg0));
            }
        });
        e.addField(new ValueAdaptor<E>("Max", clazz, upperBound) {

            @Override
            protected void writeThrough(E arg0) {
                upperBound = checkFrozen(checkNotNull(arg0));
            }
        });
        e.addField(new ValueAdaptor<E>("Initial", clazz, initialValue) {

            @Override
            protected void writeThrough(E arg0) {
                initialValue = checkFrozen(checkNotNull(arg0));
            }
            @Override
            public ValidationResult validate() {
                ValidationResult validationResult = new ValidationResult();
                if (!Comparables.areInOrder(lowerBound, initialValue, upperBound))
                    validationResult.addError("Value of `Initial' must not be smaller than `Min' and greater than `Max'");
                return validationResult;
            }
        });
    }

    @Override
    public void checkIfFreezable(Iterable<? extends GFComponent> components) throws IllegalStateException {
        super.checkIfFreezable(components);
        checkState(lowerBound != null);
        checkState(upperBound != null);
        checkState(initialValue != null);
        checkState(Comparables.areInOrder(lowerBound, initialValue, upperBound));
    }

    protected OrderedSetProperty(AbstractBuilder<? extends AbstractBuilder, E> builder) {
        super(builder);
        this.lowerBound = builder.lowerBound;
        this.upperBound = builder.upperBound;
        this.initialValue = builder.initialValue;
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T, E>, E extends Comparable<E>> extends AbstractGFProperty.AbstractBuilder<T> {
        protected E upperBound;
        protected E lowerBound;
        protected E initialValue;

        public T upperBound(E upperBound) { this.upperBound = checkNotNull(upperBound); return self(); }
        public T lowerBound(E lowerBound) { this.lowerBound = checkNotNull(lowerBound); return self(); }
        public T initialValue(E initialValue) { this.initialValue = checkNotNull(initialValue); return self(); }
    }
}
