package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.PropertyValueRequest;

import java.io.Serializable;

public abstract class AbstractAgentProperty<C, T>
        extends AbstractAgentComponent<C> implements AgentProperty<C, T> {

    protected AbstractAgentProperty(final String name) {
        super(name);
    }

    protected AbstractAgentProperty(final AbstractBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public <T> T tell(final C context, final Object message, final Class<T> replyType) {
        if (message instanceof PropertyValueRequest) {
            return replyType.cast(value(context));
        } else {
            return super.tell(context, message, replyType);
        }
    }

    protected abstract static class AbstractBuilder<P extends AbstractAgentProperty<?, ?>, B extends AbstractBuilder<P, B>>
            extends AbstractAgentComponent.AbstractBuilder<P, B> implements Serializable {
        protected AbstractBuilder(final AbstractAgentProperty<?, ?> abstractAgentProperty) {
            super(abstractAgentProperty);
        }

        protected AbstractBuilder() {
        }
    }
}
