package org.asoem.greyfish.core.properties;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import org.asoem.greyfish.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


public abstract class AbstractRangeElementProperty<E extends Number & Comparable<E>, A extends Agent<?>, C> extends AbstractAgentProperty<C, E> implements RangeElementProperty<A, E, C> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRangeElementProperty.class);

    protected E upperBound;

    protected E lowerBound;

    protected E initialValue;

    protected E value;
    @Nullable
    private A agent;

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

        if (Objects.equal(value, amount)) {
            return;
        }

        if (Ordering.<E>natural().isOrdered(ImmutableList.of(lowerBound, amount, upperBound))) {
            this.value = amount;
        } else {
            this.value = lowerBound;
            logger.debug("{} #checkAndSet({}): Out of range [{},{}]", this.getClass().getSimpleName(), amount, lowerBound, upperBound);
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

    /**
     * @return this components optional {@code Agent}
     */
    public final Optional<A> agent() {
        return Optional.fromNullable(agent);
    }

    public final void setAgent(@Nullable final A agent) {
        this.agent = agent;
    }

    protected static abstract class AbstractBuilder<A extends Agent<?>, C extends AbstractRangeElementProperty<?, A, AC>, T extends AbstractBuilder<A, C, T, E, AC>, E extends Comparable<E>, AC> extends AbstractAgentProperty.AbstractBuilder<C, T> {
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
