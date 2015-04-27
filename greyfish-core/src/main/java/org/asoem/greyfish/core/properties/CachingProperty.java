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

package org.asoem.greyfish.core.properties;

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

public final class CachingProperty<A extends Agent<? extends BasicContext<?, A>>, T, C extends AgentContext<A>> extends AbstractAgentProperty<C, T> {

    private final Callback<? super CachingProperty<A, T, C>, ? extends T> valueCallback;

    private final Callback<? super CachingProperty<A, T, C>, Boolean> expirationCallback;

    private final SingleElementCache<T> valueCache;

    private long lastModificationStep = -1;

    @Nullable
    private A agent;

    private CachingProperty(final AbstractBuilder<T, A, ? extends CachingProperty<A, T, C>, ? extends Builder<T, A, C>, C> builder) {
        super(builder);
        this.valueCallback = builder.valueCallback;
        this.expirationCallback = builder.expirationCallback;
        this.valueCache = SingleElementCache.memoize(new Supplier<T>() {
            @Override
            public T get() {
                return valueCallback.apply(CachingProperty.this, ImmutableMap.<String, Object>of());
            }
        });
    }

    @Override
    public T value(final C context) {
        if (expirationCallback.apply(CachingProperty.this, ImmutableMap.<String, Object>of())) {
            valueCache.invalidate();
            lastModificationStep = agent().get().getContext().get().getTime();
        }
        return valueCache.get();
    }

    @Override
    public void initialize() {
        super.initialize();
        lastModificationStep = -1;
    }

    public Callback<? super CachingProperty<A, T, C>, ? extends T> getValueCallback() {
        return valueCallback;
    }

    public static <T, A extends Agent<? extends BasicContext<?, A>>, C extends AgentContext<A>> Builder<T, A, C> builder() {
        return new Builder<>();
    }

    public long getLastModificationStep() {
        return lastModificationStep;
    }

    /**
     * @return this components optional {@code Agent}
     */
    public Optional<A> agent() {
        return Optional.fromNullable(agent);
    }

    public void setAgent(@Nullable final A agent) {
        this.agent = agent;
    }

    public static final class Builder<T, A extends Agent<? extends BasicContext<?, A>>, C extends AgentContext<A>> extends CachingProperty.AbstractBuilder<T, A, CachingProperty<A, T, C>, Builder<T, A, C>, C> implements Serializable {

        private Builder() {
        }

        private Builder(final CachingProperty<A, T, C> simulationStepProperty) {
            super(simulationStepProperty);
        }

        @Override
        protected Builder<T, A, C> self() {
            return this;
        }

        @Override
        protected CachingProperty<A, T, C> checkedBuild() {
            return new CachingProperty<A, T, C>(this);
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

    private abstract static class AbstractBuilder<T, A extends Agent<? extends BasicContext<?, A>>, P extends CachingProperty<A, T, C>, B extends AbstractBuilder<T, A, P, B, C>, C extends AgentContext<A>> extends AbstractAgentProperty.AbstractBuilder<P, B> implements Serializable {
        private Callback<? super CachingProperty<A, T, C>, ? extends T> valueCallback;

        private Callback<? super CachingProperty<A, T, C>, Boolean> expirationCallback = CachingProperty.expiresAtBirth();

        protected AbstractBuilder() {
            initVerification();
        }

        protected AbstractBuilder(final CachingProperty<A, T, C> simulationStepProperty) {
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

        public B value(final Callback<? super CachingProperty<A, T, C>, ? extends T> valueCallback) {
            this.valueCallback = checkNotNull(valueCallback);
            return self();
        }

        public B expires(final Callback<? super CachingProperty<A, T, C>, Boolean> expirationCallback) {
            this.expirationCallback = checkNotNull(expirationCallback);
            return self();
        }
    }

    private Object writeReplace() {
        return new Builder<>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static Callback<CachingProperty<?, ?, ?>, Boolean> expiresAtBirth() {
        return BirthExpirationCallback.INSTANCE;
    }

    private enum BirthExpirationCallback implements Callback<CachingProperty<?, ?, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(final CachingProperty<?, ?, ?> caller, final Map<String, ?> args) {
            final Agent<? extends BasicContext<?, ?>> agent = caller.agent().get();
            return caller.getLastModificationStep() < agent.getContext().get().getActivationStep();
        }
    }

    public static Callback<CachingProperty<?, ?, ?>, Boolean> expiresEveryStep() {
        return StepExpirationCallback.INSTANCE;
    }

    private enum StepExpirationCallback implements Callback<CachingProperty<?, ?, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(final CachingProperty<?, ?, ?> caller, final Map<String, ?> args) {
            final Agent<? extends BasicContext<?, ?>> agent = caller.agent().get();
            return caller.getLastModificationStep() != agent.getContext().get().getTime();
        }
    }
}