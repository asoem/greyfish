package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.SingleElementCache;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 09.05.12
 * Time: 11:29
 */
public class CachingProperty<A extends Agent<A, ?>, T> extends AbstractAgentProperty<T, A> {

    private final Callback<? super CachingProperty<A, T>, T> valueCallback;

    private final Callback<? super CachingProperty<A, T>, Boolean> expirationCallback;

    private final SingleElementCache<T> valueCache;

    private int lastModificationStep = -1;

    private CachingProperty(CachingProperty<A, T> simulationStepProperty, DeepCloner cloner) {
        super(simulationStepProperty, cloner);
        this.valueCallback = simulationStepProperty.valueCallback;
        this.expirationCallback = simulationStepProperty.expirationCallback;
        this.valueCache = SingleElementCache.memoize(new Supplier<T>() {
            @Override
            public T get() {
                return valueCallback.apply(CachingProperty.this, ImmutableMap.<String, Object>of());
            }
        });
    }

    private CachingProperty(AbstractBuilder<T, A, ? extends CachingProperty<A, T>, ? extends Builder<T, A>> builder) {
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
    public T getValue() {
        if (expirationCallback.apply(CachingProperty.this, ImmutableMap.<String, Object>of())) {
            valueCache.invalidate();
            valueCache.update();
            lastModificationStep = agent().getSimulationStep();
        }
        return valueCache.get();
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new CachingProperty<A, T>(this, cloner);
    }

    @Override
    public void initialize() {
        super.initialize();
        lastModificationStep = -1;
    }

    public Callback<? super CachingProperty<A, T>, T> getValueCallback() {
        return valueCallback;
    }

    public static <T, A extends Agent<A, ?>> Builder<T, A> builder() {
        return new Builder<T, A>();
    }

    public int getLastModificationStep() {
        return lastModificationStep;
    }

    public static class Builder<T, A extends Agent<A, ?>> extends CachingProperty.AbstractBuilder<T, A, CachingProperty<A, T>, Builder<T, A>> implements Serializable {

        private Builder() {}

        private Builder(CachingProperty<A, T> simulationStepProperty) {
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

    private abstract static class AbstractBuilder<T, A extends Agent<A, ?>, P extends CachingProperty<A, T>, B extends AbstractBuilder<T, A, P, B>> extends AbstractAgentProperty.AbstractBuilder<P, A, B> implements Serializable {
        private Callback<? super CachingProperty<A, T>, T> valueCallback;

        private Callback<? super CachingProperty<A, T>, Boolean> expirationCallback = CachingProperty.expiresAtBirth();

        protected AbstractBuilder() {}

        protected AbstractBuilder(CachingProperty<A, T> simulationStepProperty) {
            super(simulationStepProperty);
            this.valueCallback = simulationStepProperty.valueCallback;
        }

        public B value(Callback<? super CachingProperty<A, T>, T> valueCallback) {
            this.valueCallback = checkNotNull(valueCallback);
            return self();
        }

        public B expires(Callback<? super CachingProperty<A, T>, Boolean> expirationCallback) {
            this.expirationCallback = checkNotNull(expirationCallback);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(this.valueCallback != null, "No valueCallback has been defined");
        }
    }

    private Object writeReplace() {
        return new Builder<T, A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static Callback<CachingProperty<?, ?>, Boolean> expiresAtBirth() {
        return BirthExpirationCallback.INSTANCE;
    }

    private enum BirthExpirationCallback implements Callback<CachingProperty<?, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(CachingProperty<?,?> caller, Map<String, ?> args) {
            final Agent<?,?> agent = caller.agent();
            return caller.getLastModificationStep() < agent.getTimeOfBirth();
        }
    }

    public static Callback<CachingProperty<?, ?>, Boolean> expiresEveryStep() {
        return StepExpirationCallback.INSTANCE;
    }

    private enum StepExpirationCallback implements Callback<CachingProperty<?, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(CachingProperty<?,?> caller, Map<String, ?> args) {
            final Agent<?,?> agent = caller.agent();
            return caller.getLastModificationStep() != agent.getSimulationStep();
        }
    }
}