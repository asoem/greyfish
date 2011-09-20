package org.asoem.greyfish.core.properties;

import com.google.common.collect.Ordering;
import com.jgoodies.validation.ValidationResult;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


public abstract class  AbstractWellOrderedSetElementProperty<E extends Number & Comparable<E>> extends AbstractGFProperty implements WellOrderedSetElementProperty<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWellOrderedSetElementProperty.class);
    @Element(name="max")
    protected E upperBound;

    @Element(name="min")
    protected E lowerBound;

    @Element(name="init")
    protected E initialValue;

    protected E value;

    protected AbstractWellOrderedSetElementProperty(AbstractWellOrderedSetElementProperty<E> property, DeepCloner cloner) {
        super(property, cloner);
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
            LOGGER.debug("{} #setValue({}): Out of range [{},{}]", this.getClass().getSimpleName(), amount, lowerBound, upperBound);
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
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        setValue(initialValue);
    }

    public void configure(ConfigurationHandler e, Class<E> clazz) {
        super.configure(e);

        e.add(new ValueAdaptor<E>("lowerBound", clazz) {
            @Override protected void set(E arg0) { lowerBound = checkNotNull(arg0); }
            @Override public E get() { return lowerBound; }
        });
        e.add(new ValueAdaptor<E>("upperBound", clazz) {
            @Override protected void set(E arg0) { upperBound = arg0; }
            @Override public E get() { return upperBound; }
        });
        e.add(new ValueAdaptor<E>("Initial", clazz) {

            @Override protected void set(E arg0) { initialValue = arg0; }
            @Override public E get() { return initialValue; }
            @Override public ValidationResult validate() {

                ValidationResult validationResult = new ValidationResult();

                if (get() == null)
                    validationResult.addError(getName() + " must not be null");

                if (!Ordering.natural().isOrdered(Arrays.asList(lowerBound, initialValue, upperBound)))
                    validationResult.addError("Value of `"+getName()+"' must not be smaller than `Min' and greater than `Max'");

                return validationResult;
            }
        });
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

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();

            checkState(lowerBound != null);
            checkState(upperBound != null);
            checkState(initialValue != null);
            checkState(Ordering.natural().isOrdered(Arrays.asList(lowerBound, initialValue, upperBound)));
        }
    }
}
