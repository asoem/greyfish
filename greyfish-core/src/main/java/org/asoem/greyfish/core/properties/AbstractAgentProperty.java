package org.asoem.greyfish.core.properties;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.TypedSupplier;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractAgentProperty<T, A extends Agent<A, ?>> extends AbstractAgentComponent<A> implements AgentProperty<A, T> {

    @Nullable
    private A agent;

    protected AbstractAgentProperty(final AbstractBuilder<? extends AbstractAgentProperty<T, A>, A, ? extends AbstractBuilder<?, A, ?>> builder) {
        super(builder);
    }

    @Override
    public Iterable<AgentNode> children() {
        return Collections.emptyList();
    }

    @Override
    public void set(final T value) {
        throw new UnsupportedOperationException(
                "This property does not support a value modification through the set method");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void copyFrom(final TypedSupplier<?> supplier) {
        checkArgument(getValueType().isAssignableFrom(supplier.getValueType()));
        set((T) supplier.get());
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

    protected static abstract class AbstractBuilder<C extends AbstractAgentProperty<?, A>, A extends Agent<A, ?>, B extends AbstractBuilder<C, A, B>> extends AbstractAgentComponent.AbstractBuilder<A, C, B> implements Serializable {
        protected AbstractBuilder(final AbstractAgentProperty<?, A> abstractAgentProperty) {
            super(abstractAgentProperty);
        }

        public AbstractBuilder() {
        }
    }
}
