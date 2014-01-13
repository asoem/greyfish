package org.asoem.greyfish.core.properties;

import com.google.common.collect.Ordering;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.ComponentMessage;
import org.asoem.greyfish.utils.base.Tagged;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.primitives.Doubles.asList;

@Tagged("properties")
public class DoubleProperty<A extends Agent<?>, C> extends AbstractRangeElementProperty<Double, A, C> implements AgentProperty<C, Double> {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public DoubleProperty() {
        this(new Builder<A, C>());
    }

    public DoubleProperty(final Builder<A, C> builder) {
        super(builder);
    }

    public void subtract(final double val) {
        checkAndSet(value - val);
    }

    public void add(final Double val) {
        checkAndSet(value + val);
    }

    @Override
    public Double value(final C context) {
        return value;
    }

    public static <A extends Agent<?>, C extends AgentContext<A>> Builder<A, C> with() {
        return new Builder<A, C>();
    }

    public static final class Builder<A extends Agent<?>, C> extends AbstractBuilder<A, DoubleProperty<A, C>, Builder<A, C>, C> {
        public Builder() {
            lowerBound(0.0).upperBound(0.0).initialValue(0.0);
        }

        @Override
        protected Builder<A, C> self() {
            return this;
        }

        @Override
        public DoubleProperty<A, C> checkedBuild() {
            checkState(lowerBound != null);
            checkState(upperBound != null);
            checkState(initialValue != null);
            checkState(Ordering.<Comparable<Double>>natural().isOrdered(asList(lowerBound, initialValue, upperBound)));
            return new DoubleProperty<A, C>(this);
        }
    }

    protected static abstract class AbstractBuilder<A extends Agent<?>, E extends DoubleProperty<A, C>, T extends AbstractBuilder<A, E, T, C>, C> extends AbstractRangeElementProperty.AbstractBuilder<A, E, T, Double, C> {
    }

    @Override
    public <T> T tell(final C context, final Object message, final Class<T> replyType) {
        if (message instanceof Add) {
            Add add = (Add) message;
            add(add.toAdd);
            return null;
        } else {
            return super.tell(context, message, replyType);
        }
    }

    public static class Add implements ComponentMessage {
        private final String componentName;
        private final double toAdd;

        public Add(final String componentName, final double toAdd) {
            this.componentName = componentName;
            this.toAdd = toAdd;
        }

        @Override
        public String componentName() {
            return componentName;
        }
    }
}
