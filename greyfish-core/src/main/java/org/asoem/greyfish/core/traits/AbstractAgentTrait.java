package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.properties.AbstractAgentProperty;
import org.asoem.greyfish.impl.agent.TraitMutateValueRequest;
import org.asoem.greyfish.utils.collect.Product2;

import java.util.Collections;
import java.util.List;

public abstract class AbstractAgentTrait<C, T> extends AbstractAgentProperty<C, T> implements AgentTrait<C, T> {

    protected AbstractAgentTrait(final String name) {
        super(name);
    }

    protected AbstractAgentTrait(final AbstractBuilder<?, ?> builder) {
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
    public <R> R ask(final C context, final Object message, final Class<R> replyType) {
        if (message instanceof TraitMutateValueRequest) {
            final TraitMutateValueRequest mutateRequest = (TraitMutateValueRequest) message;
            final T value = (T) mutateRequest.getValue();
            return replyType.cast(transform(context, value));
        } else if (message instanceof TraitRecombineRequest) {
            final TraitRecombineRequest<T> traitRecombineRequest = (TraitRecombineRequest) message;
            final Product2<T, T> transform = transform(context, traitRecombineRequest.getValue1(), traitRecombineRequest.getValue2());
            return replyType.cast(transform);
        } else {
            return super.ask(context, message, replyType);
        }
    }
}
