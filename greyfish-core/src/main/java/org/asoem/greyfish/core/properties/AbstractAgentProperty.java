package org.asoem.greyfish.core.properties;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.agent.RequestPropertyValue;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;

public abstract class AbstractAgentProperty<T, A extends Agent<?>, C extends AgentContext<A>> extends AbstractAgentComponent<C> implements AgentProperty<C, T> {

    @Nullable
    private A agent;

    protected AbstractAgentProperty(final AbstractBuilder<? extends AbstractAgentProperty<T, A, C>, A, ? extends AbstractBuilder<?, A, ?>> builder) {
        super(builder);
    }

    @Override
    public Iterable<AgentNode> children() {
        return Collections.emptyList();
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

    @Override
    public <T> T tell(final C context, final Object message, final Class<T> replyType) {
        if (message instanceof RequestPropertyValue) {
            A agent = agent().orNull();
            setAgent(context.agent());
            T reply = replyType.cast(value(context));
            setAgent(agent);
            return reply;
        }

        throw new IllegalArgumentException();
    }

    protected static abstract class AbstractBuilder<C extends AbstractAgentProperty<?, A, ?>, A extends Agent<?>, B extends AbstractBuilder<C, A, B>> extends AbstractAgentComponent.AbstractBuilder<C, B> implements Serializable {
        protected AbstractBuilder(final AbstractAgentProperty<?, A, ?> abstractAgentProperty) {
            super(abstractAgentProperty);
        }

        public AbstractBuilder() {
        }
    }
}
