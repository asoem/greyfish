package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;

import javax.annotation.Nullable;
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
@Deprecated
public class SimulationStepProperty<A extends Agent<A, ?>, T> extends AbstractAgentProperty<T, A> {

    private Callback<? super SimulationStepProperty<A, T>, T> callback;

    private Supplier<T> value;

    private SimulationStepProperty(SimulationStepProperty<A, T> simulationStepProperty, DeepCloner cloner) {
        super(simulationStepProperty, cloner);
        this.callback = simulationStepProperty.callback;
    }

    private SimulationStepProperty(AbstractBuilder<T, A, ? extends SimulationStepProperty<A, T>, ? extends Builder<T, A>> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    public T getValue() {
        checkState(value != null, "Property was not initialized");
        return value.get();
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new SimulationStepProperty<A, T>(this, cloner);
    }

    @Override
    public void initialize() {
        super.initialize();
        checkState(callback != null, "Callback is null");
        value = MoreSuppliers.memoize(
                new Supplier<T>() {
                    @Override
                    public T get() {
                        assert callback != null;
                        return callback.apply(SimulationStepProperty.this, ArgumentMap.of());
                    }
                },
                new UpdateRequest<T>() {
                    private int stepForValue = -1;

                    @Override
                    public void updated() {
                        stepForValue = agent().getSimulationStep();
                    }

                    @Override
                    public boolean isOutdated(@Nullable T input) {
                        return agent().getSimulationStep() > stepForValue;
                    }
                }
        );
    }


    public Callback<? super SimulationStepProperty<A, T>, T> getCallback() {
        return callback;
    }

    public static <T, A extends Agent<A, ?>> Builder<T, A> builder() {
        return new Builder<T, A>();
    }

    public static class Builder<T, A extends Agent<A, ?>> extends SimulationStepProperty.AbstractBuilder<T, A, SimulationStepProperty<A, T>, Builder<T, A>> implements Serializable {

        private Builder() {}

        private Builder(SimulationStepProperty<A, T> simulationStepProperty) {
            super(simulationStepProperty);
        }

        @Override
        protected Builder<T, A> self() {
            return this;
        }

        @Override
        protected SimulationStepProperty<A, T> checkedBuild() {
            return new SimulationStepProperty<A, T>(this);
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

    private abstract static class AbstractBuilder<T, A extends Agent<A, ?>, P extends SimulationStepProperty<A, T>, B extends AbstractBuilder<T, A, P, B>> extends AbstractAgentProperty.AbstractBuilder<P, A, B> implements Serializable {
        private Callback<? super SimulationStepProperty<A, T>, T> callback;

        protected AbstractBuilder() {}

        protected AbstractBuilder(SimulationStepProperty<A, T> simulationStepProperty) {
            super(simulationStepProperty);
            this.callback = simulationStepProperty.callback;
        }

        public B callback(Callback<? super SimulationStepProperty<A, T>, T> function) {
            this.callback = checkNotNull(function);
            return self();
        }
    }

    private Object writeReplace() {
        return new Builder<T, A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }
}