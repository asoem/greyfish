package org.asoem.greyfish.core.traits;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
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
public class ContextualTrait<A extends Agent<A, ?>, T> extends AbstractTrait<A, T> {

    private final Callback<? super ContextualTrait<A, T>, ? extends T> valueCallback;

    private final Callback<? super ContextualTrait<A, T>, ? extends Boolean> expirationCallback;

    private final SingleElementCache<T> valueCache;

    private int lastModificationStep = -1;

    private ContextualTrait(ContextualTrait<A, T> simulationStepProperty, DeepCloner cloner) {
        super(simulationStepProperty, cloner);
        this.valueCallback = simulationStepProperty.valueCallback;
        this.expirationCallback = simulationStepProperty.expirationCallback;
        this.valueCache = SingleElementCache.memoize(new Supplier<T>() {
            @Override
            public T get() {
                return valueCallback.apply(ContextualTrait.this, ImmutableMap.<String, Object>of());
            }
        });
    }

    private ContextualTrait(AbstractBuilder<T, A, ? extends ContextualTrait<A, T>, ? extends Builder<T, A>> builder) {
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
    public TypeToken<T> getValueType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get() {
        if (expirationCallback.apply(ContextualTrait.this, ImmutableMap.<String, Object>of())) {
            valueCache.invalidate();
            valueCache.update();
            lastModificationStep = agent().getSimulationStep();
        }
        return valueCache.get();
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ContextualTrait<A, T>(this, cloner);
    }

    @Override
    public void initialize() {
        super.initialize();
        lastModificationStep = -1;
    }

    public Callback<? super ContextualTrait<A, T>, ? extends T> getValueCallback() {
        return valueCallback;
    }

    public static <T, A extends Agent<A, ?>> Builder<T, A> builder() {
        return new Builder<T, A>();
    }

    public int getLastModificationStep() {
        return lastModificationStep;
    }

    @Override
    public T createInitialValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHeritable() {
        return false;
    }

    public static class Builder<T, A extends Agent<A, ?>> extends ContextualTrait.AbstractBuilder<T, A, ContextualTrait<A, T>, Builder<T, A>> implements Serializable {

        private Builder() {}

        private Builder(ContextualTrait<A, T> contextualTrait) {
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

    private abstract static class AbstractBuilder<T, A extends Agent<A, ?>, P extends ContextualTrait<A, T>, B extends AbstractBuilder<T, A, P, B>> extends AbstractTrait.AbstractBuilder<A, P, B> implements Serializable {
        private Callback<? super ContextualTrait<A, T>, ? extends T> valueCallback;

        private Callback<? super ContextualTrait<A, T>, ? extends Boolean> expirationCallback = ContextualTrait.expiresAtBirth();

        protected AbstractBuilder() {}

        protected AbstractBuilder(ContextualTrait<A, T> simulationStepProperty) {
            super(simulationStepProperty);
            this.valueCallback = simulationStepProperty.valueCallback;
        }

        public B value(Callback<? super ContextualTrait<A, T>, ? extends T> valueCallback) {
            this.valueCallback = checkNotNull(valueCallback);
            return self();
        }

        public B expires(Callback<? super ContextualTrait<A, T>, ? extends Boolean> expirationCallback) {
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

    public static Callback<ContextualTrait<?, ?>, Boolean> expiresAtBirth() {
        return BirthExpirationCallback.INSTANCE;
    }

    private enum BirthExpirationCallback implements Callback<ContextualTrait<?, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(ContextualTrait<?,?> caller, Map<String, ?> args) {
            final Agent<?,?> agent = caller.agent();
            return caller.getLastModificationStep() < agent.getTimeOfBirth();
        }
    }

    public static Callback<ContextualTrait<?, ?>, Boolean> expiresEveryStep() {
        return StepExpirationCallback.INSTANCE;
    }

    private enum StepExpirationCallback implements Callback<ContextualTrait<?, ?>, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(ContextualTrait<?,?> caller, Map<String, ?> args) {
            final Agent<?,?> agent = caller.agent();
            return caller.getLastModificationStep() != agent.getSimulationStep();
        }
    }
}