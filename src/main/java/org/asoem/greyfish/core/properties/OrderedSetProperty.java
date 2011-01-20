package org.asoem.greyfish.core.properties;

import com.jgoodies.validation.ValidationResult;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;


public abstract class OrderedSetProperty<E extends Comparable<E>> extends AbstractGFProperty implements DiscreteProperty<E> {

    @Element(name="min")
    protected E upperBound;

    @Element(name="max")
    protected E lowerBound;

    @Element(name="init")
    protected E initialValue;

    protected E value;

    @Override
    public E getValue() {
        return value;
    }

    public void setValue(E amount) {
        if (rangeCheck(lowerBound, upperBound, amount)) {
            this.value = amount;
            firePropertyChanged();
        }
        else
        if (GreyfishLogger.isDebugEnabled())
            GreyfishLogger.debug(this.getClass().getSimpleName() + "#setValue("+amount+"): Out of range ("+lowerBound+","+upperBound+")");
    }

    public E getUpperBound() {
        return upperBound;
    }

    public E getLowerBound() {
        return lowerBound;
    }

    public E getInitialValue() {
        return initialValue;
    }

    private boolean rangeCheck(E from, E to, E value) {
        return from.compareTo( value ) <= 0
                && to.compareTo( value ) >= 0;
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
                lowerBound = arg0;
            }
        });
        e.addField(new ValueAdaptor<E>("Max", clazz, upperBound) {

            @Override
            protected void writeThrough(E arg0) {
                upperBound = arg0;
            }
        });
        e.addField(new ValueAdaptor<E>("Initial", clazz, initialValue) {

            @Override
            protected void writeThrough(E arg0) {
                initialValue = arg0;
            }
            @Override
            public ValidationResult validate() {
                ValidationResult validationResult = new ValidationResult();
                if (!rangeCheck(lowerBound, upperBound, initialValue))
                    validationResult.addError("Value of `Initial' must not be smaller than `Min' and greater than `Max'");
                return validationResult;
            }
        });
    }

    protected OrderedSetProperty(AbstractBuilder<? extends AbstractBuilder, E> builder) {
        super(builder);
    }

    public static final class Builder<E extends Comparable<E>> extends AbstractBuilder<Builder<E>, E> {
        @Override protected Builder<E> self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T, E>, E extends Comparable<E>> extends AbstractGFProperty.AbstractBuilder<T> {
        protected E upperBound;
        protected E lowerBound;
        protected E initialValue;

        public T upperBound(E upperBound) { this.upperBound = upperBound; return self(); }
        public T lowerBound(E lowerBound) { this.lowerBound = lowerBound; return self(); }
        public T initialValue(E initialValue) { this.initialValue = initialValue; return self(); }

        protected T fromClone(OrderedSetProperty<E> property, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(property, mapDict).
                    lowerBound(property.lowerBound).
                    upperBound(property.upperBound).
                    initialValue(property.initialValue);
            return self();
        }
    }
}
