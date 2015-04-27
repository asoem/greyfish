/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
