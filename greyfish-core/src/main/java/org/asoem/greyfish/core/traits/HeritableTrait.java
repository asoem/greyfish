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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.properties.AbstractAgentProperty;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Tuple2;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A generic heritable trait which holds an arbitrary value of type {@code T}. Initialization, mutation and segregation
 * of this value are defined using {@link Callback}s.
 *
 * @param <A> the type of the enclosing {@link Agent}
 * @param <T> the type of the value of this trait
 */
public class HeritableTrait<A extends Agent<?>, T, C extends AgentContext<A>> extends AbstractAgentTrait<C, T> implements AgentTrait<C, T> {

    private final TypeToken<T> typeToken;

    private final Callback<? super HeritableTrait<A, T, C>, T> initializationKernel;

    private final Callback<? super HeritableTrait<A, T, C>, T> mutationKernel;

    private final Callback<? super HeritableTrait<A, T, C>, T> segregationKernel;

    @Nullable
    private T value;
    @Nullable
    private A agent;

    private HeritableTrait(final AbstractBuilder<A, ? extends HeritableTrait<A, T, C>, ? extends AbstractBuilder<A, ?, ?, T, C>, T, C> builder) {
        super(builder);
        this.initializationKernel = checkNotNull(builder.initializationKernel);
        this.mutationKernel = checkNotNull(builder.mutationKernel);
        this.segregationKernel = checkNotNull(builder.segregationKernel);
        this.typeToken = checkNotNull(builder.typeToken);
        this.value = builder.value;
    }

    public Callback<? super HeritableTrait<A, T, C>, T> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super HeritableTrait<A, T, C>, T> getMutationKernel() {
        return mutationKernel;
    }

    @Override
    public T transform(final C context, final T value) {
        return mutationKernel.apply(this, ImmutableMap.of("x", value));
    }

    @Override
    public Product2<T, T> transform(final C context, final T allele1, final T allele2) {
        T apply = segregationKernel.apply(this, ImmutableMap.of("x", allele1, "y", allele2));
        return Tuple2.of(apply, apply);
    }

    @Nullable
    public T value(final C context) {
        return value;
    }

    public Callback<? super HeritableTrait<A, T, C>, T> getSegregationKernel() {
        return segregationKernel;
    }

    public static <A extends Agent<?>, C extends AgentContext<A>, T> Builder<A, T, C> builder() {
        return new Builder<>();
    }

    private Object writeReplace() {
        return new Builder<A, T, C>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    /**
     * @return this components optional {@code Agent}
     */
    public Optional<A> agent() {
        return Optional.fromNullable(agent);
    }

    public final void setAgent(@Nullable final A agent) {
        this.agent = agent;
    }

    public static class Builder<A extends Agent<?>, T, AC extends AgentContext<A>> extends AbstractBuilder<A, HeritableTrait<A, T, AC>, Builder<A, T, AC>, T, AC> implements Serializable {
        private Builder() {
        }

        private Builder(final HeritableTrait<A, T, AC> quantitativeTrait) {
            super(quantitativeTrait);
        }

        @Override
        protected Builder<A, T, AC> self() {
            return this;
        }

        @Override
        protected HeritableTrait<A, T, AC> checkedBuild() {
            return new HeritableTrait<A, T, AC>(this);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed: " + e);
            }
        }

        private static final long serialVersionUID = 0;
    }

    protected abstract static class AbstractBuilder<A extends Agent<?>, C extends HeritableTrait<A, T, AC>, B extends AbstractBuilder<A, C, B, T, AC>, T, AC extends AgentContext<A>>
            extends AbstractAgentProperty.AbstractBuilder<C, B> implements Serializable {

        private final Callback<Object, T> defaultInitializationKernel = Callbacks.willThrow(new UnsupportedOperationException());
        private final Callback<Object, T> defaultMutationKernel = Callbacks.willThrow(new UnsupportedOperationException());
        private final Callback<Object, T> defaultSegregationKernel = Callbacks.willThrow(new UnsupportedOperationException());

        private Callback<? super HeritableTrait<A, T, AC>, T> initializationKernel = defaultInitializationKernel;
        private Callback<? super HeritableTrait<A, T, AC>, T> mutationKernel = defaultMutationKernel;
        private Callback<? super HeritableTrait<A, T, AC>, T> segregationKernel = defaultSegregationKernel;
        @Nullable
        private T value;
        private TypeToken<T> typeToken;

        protected AbstractBuilder(final HeritableTrait<A, T, AC> quantitativeTrait) {
            this.initializationKernel = quantitativeTrait.initializationKernel;
            this.mutationKernel = quantitativeTrait.mutationKernel;
            this.segregationKernel = quantitativeTrait.segregationKernel;
            this.value = quantitativeTrait.value;
        }

        protected AbstractBuilder() {
        }

        public final B initialization(final Callback<? super AgentTrait<?, T>, T> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public final B mutation(final Callback<? super AgentTrait<?, T>, T> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public final B segregation(final Callback<? super AgentTrait<?, T>, T> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        public final B ofType(final TypeToken<T> typeToken) {
            this.typeToken = typeToken;
            return self();
        }

        // only used internally for serialization
        protected final B value(@Nullable final T value) {
            this.value = value;
            return self();
        }
    }
}
