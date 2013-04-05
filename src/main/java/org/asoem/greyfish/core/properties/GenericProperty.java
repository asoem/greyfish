package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 09.05.12
 * Time: 11:29
 */
public class GenericProperty<A extends Agent<A, ?>, T> extends AbstractAgentProperty<T, A> {

    private final Callback<? super GenericProperty<A, T>, T> valueCallback;

    private final Callback<? super GenericProperty<A, T>, Boolean> expirationCallback;

    private final SingleElementCache<T> valueSupplier;

    private int lastModificationStep = -1;

    private GenericProperty(GenericProperty<A, T> simulationStepProperty, DeepCloner cloner) {
        super(simulationStepProperty, cloner);
        this.valueCallback = simulationStepProperty.valueCallback;
        this.expirationCallback = simulationStepProperty.expirationCallback;
        this.valueSupplier = SingleElementCache.memoize(new Supplier<T>() {
            @Override
            public T get() {
                return valueCallback.apply(GenericProperty.this, ArgumentMap.of());
            }
        });
    }

    private GenericProperty(AbstractBuilder<T, A, ? extends GenericProperty<A, T>, ? extends Builder<T, A>> builder) {
        super(builder);
        this.valueCallback = builder.valueCallback;
        this.expirationCallback = builder.expirationCallback;
        this.valueSupplier = SingleElementCache.memoize(new Supplier<T>() {
            @Override
            public T get() {
                return valueCallback.apply(GenericProperty.this, ArgumentMap.of());
            }
        });
    }

    @Override
    public T getValue() {
        if (expirationCallback.apply(GenericProperty.this, ArgumentMap.of())) {
            valueSupplier.invalidate();
            valueSupplier.update();
            lastModificationStep = agent().getSimulationStep();
        }
        return valueSupplier.get();
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new GenericProperty<A, T>(this, cloner);
    }

    @Override
    public void initialize() {
        super.initialize();
        lastModificationStep = -1;
    }

    public Callback<? super GenericProperty<A, T>, T> getValueCallback() {
        return valueCallback;
    }

    public static <T, A extends Agent<A, ?>> Builder<T, A> builder() {
        return new Builder<T, A>();
    }

    public int getLastModificationStep() {
        return lastModificationStep;
    }

    public static class Builder<T, A extends Agent<A, ?>> extends GenericProperty.AbstractBuilder<T, A, GenericProperty<A, T>, Builder<T, A>> implements Serializable {

        private Builder() {}

        private Builder(GenericProperty<A, T> simulationStepProperty) {
            super(simulationStepProperty);
        }

        @Override
        protected Builder<T, A> self() {
            return this;
        }

        @Override
        protected GenericProperty<A, T> checkedBuild() {
            return new GenericProperty<A, T>(this);
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

    private abstract static class AbstractBuilder<T, A extends Agent<A, ?>, P extends GenericProperty<A, T>, B extends AbstractBuilder<T, A, P, B>> extends AbstractAgentProperty.AbstractBuilder<P, A, B> implements Serializable {
        private Callback<? super GenericProperty<A, T>, T> valueCallback;

        private Callback<? super GenericProperty<A, T>, Boolean> expirationCallback = GenericProperty.<A, T>expiresAtBirth();

        protected AbstractBuilder() {}

        protected AbstractBuilder(GenericProperty<A, T> simulationStepProperty) {
            super(simulationStepProperty);
            this.valueCallback = simulationStepProperty.valueCallback;
        }

        public B valueCallback(Callback<? super GenericProperty<A, T>, T> valueCallback) {
            this.valueCallback = checkNotNull(valueCallback);
            return self();
        }

        public B expirationCallback(Callback<? super GenericProperty<A, T>, Boolean> expirationCallback) {
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

    public static <A extends Agent<A, ?>, T> Callback<GenericProperty<A, T>, Boolean> expiresAtBirth() {
        return new Callback<GenericProperty<A, T>, Boolean>() {
            @Override
            public Boolean apply(GenericProperty<A, T> genericProperty, Arguments arguments) {
                final A agent = genericProperty.agent();
                return genericProperty.getLastModificationStep() < agent.getTimeOfBirth();
            }
        };
    }

    public static <A extends Agent<A, ?>, T> Callback<GenericProperty<A, T>, Boolean> expiresEveryStep() {
        return new Callback<GenericProperty<A, T>, Boolean>() {
            @Override
            public Boolean apply(GenericProperty<A, T> genericProperty, Arguments arguments) {
                final A agent = genericProperty.agent();
                return genericProperty.getLastModificationStep() != agent.getSimulationStep();
            }
        };
    }
}