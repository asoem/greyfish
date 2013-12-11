package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.SingleElementCache;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class CachingProperty<A extends Agent<A, ? extends BasicSimulationContext<?, A>>, T> extends AbstractAgentProperty<T, A> {

    private final Callback<? super CachingProperty<A, T>, ? extends T> valueCallback;

    private final Callback<? super CachingProperty<A, T>, ? extends Boolean> expirationCallback;

    private final SingleElementCache<T> valueCache;

    private long lastModificationStep = -1;

    private CachingProperty(final AbstractBuilder<T, A, ? extends CachingProperty<A, T>, ? extends Builder<T, A>> builder) {
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
    public TypeToken<T> getValueType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get() {
        if (expirationCallback.apply(CachingProperty.this, ImmutableMap.<String, Object>of())) {
            valueCache.invalidate();
            valueCache.update();
            lastModificationStep = agent().get().getContext().get().getTime();
        }
        return valueCache.get();
    }

    @Override
    public void initialize() {
        super.initialize();
        lastModificationStep = -1;
    }

    public Callback<? super CachingProperty<A, T>, ? extends T> getValueCallback() {
        return valueCallback;
    }

    public static <T, A extends Agent<A, ? extends BasicSimulationContext<?, A>>> Builder<T, A> builder() {
        return new Builder<>();
    }

    public long getLastModificationStep() {
        return lastModificationStep;
    }

    public static class Builder<T, A extends Agent<A, ? extends BasicSimulationContext<?, A>>> extends CachingProperty.AbstractBuilder<T, A, CachingProperty<A, T>, Builder<T, A>> implements Serializable {

        private Builder() {
        }

        private Builder(final CachingProperty<A, T> simulationStepProperty) {
            super(simulationStepProperty);
        }

        @Override
        protected Builder<T, A> self() {
            return this;
        }

        @Override
        protected CachingProperty<A, T> checkedBuild() {
            return new CachingProperty<A, T>(this);
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

    private abstract static class AbstractBuilder<T, A extends Agent<A, ? extends BasicSimulationContext<?, A>>, P extends CachingProperty<A, T>, B extends AbstractBuilder<T, A, P, B>> extends AbstractAgentProperty.AbstractBuilder<P, A, B> implements Serializable {
        private Callback<? super CachingProperty<A, T>, ? extends T> valueCallback;

        private Callback<? super CachingProperty<A, T>, ? extends Boolean> expirationCallback = CachingProperty.expiresAtBirth();

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(final CachingProperty<A, T> simulationStepProperty) {
            super(simulationStepProperty);
            this.valueCallback = simulationStepProperty.valueCallback;
        }

        public B value(final Callback<? super CachingProperty<A, T>, ? extends T> valueCallback) {
            this.valueCallback = checkNotNull(valueCallback);
            return self();
        }

        public B expires(final Callback<? super CachingProperty<A, T>, ? extends Boolean> expirationCallback) {
            this.expirationCallback = checkNotNull(expirationCallback);
            return self();
        }

        @Override
        protected void checkBuilder() {
            checkState(this.valueCallback != null, "No valueCallback has been defined");
        }
    }

    private Object writeReplace() {
        return new Builder<>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static Callback<CachingProperty<?, ?>, Boolean> expiresAtBirth() {
        return BirthExpirationCallback.INSTANCE;
    }

    private enum BirthExpirationCallback implements Callback<CachingProperty<?, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(final CachingProperty<?, ?> caller, final Map<String, ?> args) {
            final Agent<?, ? extends BasicSimulationContext<?, ?>> agent = caller.agent().get();
            return caller.getLastModificationStep() < agent.getContext().get().getActivationStep();
        }
    }

    public static Callback<CachingProperty<?, ?>, Boolean> expiresEveryStep() {
        return StepExpirationCallback.INSTANCE;
    }

    private enum StepExpirationCallback implements Callback<CachingProperty<?, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(final CachingProperty<?, ?> caller, final Map<String, ?> args) {
            final Agent<?, ? extends BasicSimulationContext<?, ?>> agent = caller.agent().get();
            return caller.getLastModificationStep() != agent.getContext().get().getTime();
        }
    }
}