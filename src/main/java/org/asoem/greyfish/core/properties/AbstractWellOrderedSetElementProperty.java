package org.asoem.greyfish.core.properties;

import com.google.common.collect.Ordering;
import com.jgoodies.validation.ValidationResult;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.io.GreyfishLogger.CORE_LOGGER;


public abstract class AbstractWellOrderedSetElementProperty<E extends Number & Comparable<E>> extends AbstractGFProperty implements WellOrderedSetElementProperty<E> {

    @Element(name="max")
    protected E upperBound;

    @Element(name="min")
    protected E lowerBound;

    @Element(name="init")
    protected E initialValue;

    protected E value;

    protected AbstractWellOrderedSetElementProperty(AbstractWellOrderedSetElementProperty<E> property, CloneMap cloneMap) {
        super(property, cloneMap);
        this.lowerBound = property.lowerBound;
        this.upperBound = property.upperBound;
        this.initialValue = property.initialValue;
    }

    @Override
    public E get() {
        return value;
    }

    public void setValue(E amount) {
        checkNotNull(amount);
        if (Ordering.natural().isOrdered(Arrays.asList(lowerBound, amount, upperBound))) {
            this.value = amount;
        }
        else {
            this.value = lowerBound;
            if (CORE_LOGGER.isDebugEnabled())
                CORE_LOGGER.debug(this.getClass().getSimpleName() + "#setValue(" + amount + "): Out of range [" + lowerBound + ", " + upperBound + "]");
        }
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
        e.add(new ValueAdaptor<E>("lowerBound", clazz) {
            @Override protected void set(E arg0) { lowerBound = checkFrozen(checkNotNull(arg0)); }
            @Override public E get() { return lowerBound; }
        });
        e.add(new ValueAdaptor<E>("upperBound", clazz) {
            @Override protected void set(E arg0) { upperBound = checkFrozen(checkNotNull(arg0)); }
            @Override public E get() { return upperBound; }
        });
        e.add(new ValueAdaptor<E>("Initial", clazz) {

            @Override protected void set(E arg0) { initialValue = checkFrozen(checkNotNull(arg0)); }
            @Override public E get() { return initialValue; }
            @Override public ValidationResult validate() {
                ValidationResult validationResult = new ValidationResult();
                if (!Ordering.natural().isOrdered(Arrays.asList(lowerBound, initialValue, upperBound)))
                    validationResult.addError("Value of `Initial' must not be smaller than `Min' and greater than `Max'");
                return validationResult;
            }
        });
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        super.checkConsistency(components);
        checkState(lowerBound != null);
        checkState(upperBound != null);
        checkState(initialValue != null);
        checkState(Ordering.natural().isOrdered(Arrays.asList(lowerBound, initialValue, upperBound)));
    }

    protected AbstractWellOrderedSetElementProperty(AbstractBuilder<? extends AbstractBuilder, E> builder) {
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
