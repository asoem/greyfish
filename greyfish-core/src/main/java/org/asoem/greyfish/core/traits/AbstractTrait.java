package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.agent.RequestTraitValue;
import org.asoem.greyfish.utils.collect.Product2;

import java.util.Collections;
import java.util.List;

public abstract class AbstractTrait<A extends Agent<?>, C extends AgentContext<A>, T> extends AbstractAgentComponent<C> implements AgentTrait<C, T> {

    protected AbstractTrait(final String name) {
        super(name);
    }

    protected AbstractTrait(final AbstractBuilder<? extends AbstractTrait<A, C, T>, ? extends AbstractBuilder<?, ?>> builder) {
        super(builder);
    }

    @Override
    public Iterable<AgentNode> children() {
        return Collections.emptyList();
    }

    @Override
    public Product2<T, T> transform(final C context, final T allele1, final T allele2) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<T> transform(final C context, final List<? extends T> alleles) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public T transform(final C context, final T value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T> T tell(final C context, final Object message, final Class<T> replyType) {
        if (message instanceof RequestTraitValue) {
            return replyType.cast(value(context));
        }
        throw new IllegalArgumentException("Can't handle message: " + message);
    }
}
