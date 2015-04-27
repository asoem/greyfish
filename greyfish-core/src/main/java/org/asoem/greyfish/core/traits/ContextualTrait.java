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
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicContext;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.SingleElementCache;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 *
 */
public class ContextualTrait<A extends Agent<? extends BasicContext<?, A>>, T>
        extends AbstractAgentTrait<AgentContext<A>, T> {

    private final Callback<? super ContextualTrait<A, T>, ? extends T> valueCallback;

    private final Callback<? super ContextualTrait<A, T>, Boolean> expirationCallback;

    private final SingleElementCache<T> valueCache;

    private long lastModificationStep = -1;

    @Nullable
    private A agent;

    private ContextualTrait(
            final AbstractBuilder<T, A, ? extends ContextualTrait<A, T>, ? extends Builder<T, A>> builder) {
        super(builder);
        this.valueCallback = builder.valueCallback;
        this.expirationCallback = builder.expirationCallback;
        this.valueCache = SingleElementCache.memoize(new Supplier<T>() {
            @Override
            public T get() {
                return valueCallback.apply(ContextualTrait.this, ImmutableMap.<String, Object>of());
            }
        });
    }

    @Override
    public T value(final AgentContext<A> context) {
        if (expirationCallback.apply(ContextualTrait.this, ImmutableMap.<String, Object>of())) {
            valueCache.invalidate();
            lastModificationStep = context.agent().getContext().get().getTime();
        }
        return valueCache.get();
    }

    @Override
    public void initialize() {
        super.initialize();
        lastModificationStep = -1;
    }

    public Optional<A> agent() {
        return Optional.absent();
    }

    public Callback<? super ContextualTrait<A, T>, ? extends T> getValueCallback() {
        return valueCallback;
    }

    public static <T, A extends Agent<? extends BasicContext<?, A>>> Builder<T, A> builder() {
        return new Builder<>();
    }

    public long getLastModificationStep() {
        return lastModificationStep;
    }

    public final void setAgent(@Nullable final A agent) {
        this.agent = agent;
    }

    public static class Builder<T, A extends Agent<? extends BasicContext<?, A>>>
            extends ContextualTrait.AbstractBuilder<T, A, ContextualTrait<A, T>, Builder<T, A>>
            implements Serializable {

        private Builder() {
        }

        private Builder(final ContextualTrait<A, T> contextualTrait) {
            super(contextualTrait);
        }

        @Override
        protected Builder<T, A> self() {
            return this;
        }

        @Override
        protected ContextualTrait<A, T> checkedBuild() {
            return new ContextualTrait<A, T>(this);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }

    private abstract static class AbstractBuilder<T, A extends Agent<? extends BasicContext<?, A>>,
            P extends ContextualTrait<A, T>, B extends AbstractBuilder<T, A, P, B>>
            extends AbstractAgentTrait.AbstractBuilder<P, B> implements Serializable {
        private Callback<? super ContextualTrait<A, T>, ? extends T> valueCallback;

        private Callback<? super ContextualTrait<A, T>, Boolean> expirationCallback = ContextualTrait.expiresAtBirth();

        protected AbstractBuilder() {
            initVerification();
        }

        protected AbstractBuilder(final ContextualTrait<A, T> simulationStepProperty) {
            this.valueCallback = simulationStepProperty.valueCallback;
            initVerification();
        }

        private void initVerification() {
            addVerification(new Verification() {
                @Override
                protected void verify() {
                    checkState(valueCallback != null, "No valueCallback has been defined");
                }
            });
        }

        public B value(final Callback<? super ContextualTrait<A, T>, ? extends T> valueCallback) {
            this.valueCallback = checkNotNull(valueCallback);
            return self();
        }

        public B expires(final Callback<? super ContextualTrait<A, T>, Boolean> expirationCallback) {
            this.expirationCallback = checkNotNull(expirationCallback);
            return self();
        }
    }

    private Object writeReplace() {
        return new Builder<T, A>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <T, A extends Agent<? extends BasicContext<?, A>>>
    Callback<? super ContextualTrait<A, T>, Boolean> expiresAtBirth() {
        return BirthExpirationCallback.INSTANCE;
    }

    private static enum BirthExpirationCallback
            implements Callback<ContextualTrait<? extends Agent<? extends BasicContext<?, ?>>, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(final ContextualTrait<? extends Agent<? extends BasicContext<?, ?>>, ?> caller,
                             final Map<String, ?> args) {
            final Agent<? extends BasicContext<?, ?>> agent = caller.agent().get();
            return caller.getLastModificationStep() < agent.getContext().get().getActivationStep();
        }
    }

    public static <T, A extends Agent<? extends BasicContext<?, A>>>
    Callback<? super ContextualTrait<A, T>, Boolean> expiresEveryStep() {
        return StepExpirationCallback.INSTANCE;
    }

    private static enum StepExpirationCallback
            implements Callback<ContextualTrait<? extends Agent<? extends BasicContext<?, ?>>, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(final ContextualTrait<? extends Agent<? extends BasicContext<?, ?>>, ?> caller,
                             final Map<String, ?> args) {
            final Agent<? extends BasicContext<?, ?>> agent = caller.agent().get();
            return caller.getLastModificationStep() != agent.getContext().get().getTime();
        }
    }
}