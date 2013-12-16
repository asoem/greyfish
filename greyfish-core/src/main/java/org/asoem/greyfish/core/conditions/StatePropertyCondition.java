package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.properties.FiniteStateProperty;
import org.asoem.greyfish.utils.base.Tagged;

import static com.google.common.base.Preconditions.checkNotNull;

@Tagged("conditions")
public class StatePropertyCondition<A extends Agent<A, ?>> extends LeafCondition<A> {

    private FiniteStateProperty<?, A> stateProperty;
    // TODO: The stateProperty might get modified during construction phase. Observe this!

    private Object state;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public StatePropertyCondition() {
        this(new Builder<A>());
    }

    private StatePropertyCondition(final AbstractBuilder<A, ?, ?> builder) {
        super(builder);
        this.state = builder.state;
        this.stateProperty = builder.property;
    }

    @Override
    public boolean evaluate() {
        return stateProperty != null &&
                Objects.equal(stateProperty.get(), state);
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, StatePropertyCondition<A>, Builder<A>> {
        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected StatePropertyCondition<A> checkedBuild() {
            return new StatePropertyCondition<A>(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, E extends StatePropertyCondition<A>, T extends AbstractBuilder<A, E, T>> extends LeafCondition.AbstractBuilder<A, E, T> {
        private FiniteStateProperty<?, A> property;
        private Object state;

        public T property(final FiniteStateProperty<?, A> property) {
            this.property = checkNotNull(property);
            return self();
        }

        public T hasState(final Object state) {
            this.state = checkNotNull(state);
            return self();
        }
    }
}
