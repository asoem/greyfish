package org.asoem.greyfish.core.properties;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


public abstract class AbstractRangeElementProperty<E extends Number & Comparable<E>, A extends Agent<?>, C extends AgentContext<A>> extends AbstractAgentProperty<E, A, C> implements RangeElementProperty<A, E, C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRangeElementProperty.class);

    protected E upperBound;

    protected E lowerBound;

    protected E initialValue;

    protected E value;

    protected AbstractRangeElementProperty(final AbstractBuilder<A, ? extends AbstractRangeElementProperty<E, A, C>, ?, E, C> builder) {
        super(builder);
        this.lowerBound = builder.lowerBound;
        this.upperBound = builder.upperBound;
        this.initialValue = builder.initialValue;
    }

    @Override
    public E value(final C context) {
        return value;
    }

    protected void checkAndSet(final E amount) {
        checkNotNull(amount);

        if (Objects.equal(value, amount))
            return;

        if (Ordering.<E>natural().isOrdered(ImmutableList.of(lowerBound, amount, upperBound))) {
            this.value = amount;
        } else {
            this.value = lowerBound;
            LOGGER.debug("{} #checkAndSet({}): Out of range [{},{}]", this.getClass().getSimpleName(), amount, lowerBound, upperBound);
        }
    }

    public E getInitialValue() {
        return initialValue;
    }

    @Override
    public void initialize() {
        super.initialize();
        checkAndSet(initialValue);
    }

    protected static abstract class AbstractBuilder<A extends Agent<?>, C extends AbstractRangeElementProperty<?, A, AC>, T extends AbstractBuilder<A, C, T, E, AC>, E extends Comparable<E>, AC extends AgentContext<A>> extends AbstractAgentProperty.AbstractBuilder<C, A, T> {
        protected E upperBound;
        protected E lowerBound;
        protected E initialValue;

        public T upperBound(final E upperBound) {
            this.upperBound = checkNotNull(upperBound);
            return self();
        }

        public T lowerBound(final E lowerBound) {
            this.lowerBound = checkNotNull(lowerBound);
            return self();
        }

        public T initialValue(final E initialValue) {
            this.initialValue = checkNotNull(initialValue);
            return self();
        }

        @Override
        protected void checkBuilder() {
            super.checkBuilder();

            checkState(lowerBound != null);
            checkState(upperBound != null);
            checkState(initialValue != null);
            checkState(Ordering.<E>natural().isOrdered(ImmutableList.of(lowerBound, initialValue, upperBound)));
        }
    }

    @Override
    public Range<E> getRange() {
        return Range.closed(lowerBound, upperBound);
    }
}
