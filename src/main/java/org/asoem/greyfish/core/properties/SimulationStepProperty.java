package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;

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
public class SimulationStepProperty<T, A extends Agent<A, ?, ?>> extends AbstractAgentProperty<T,A> {

    private Callback<? super SimulationStepProperty<T, A>, T> callback;

    private Supplier<T> value;

    private SimulationStepProperty(SimulationStepProperty<T, A> simulationStepProperty, DeepCloner cloner) {
        super(simulationStepProperty, cloner);
        this.callback = simulationStepProperty.callback;
    }

    private SimulationStepProperty(AbstractBuilder<T, A, ? extends SimulationStepProperty<T, A>, ? extends Builder> builder) {
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
        return new SimulationStepProperty<T, A>(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Value", TypedValueModels.forField("callback", this, new TypeToken<Callback<? super LifetimeProperty<T, A>, T>>() {
        }));
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
                    public void done() {
                        stepForValue = agent().getSimulationStep();
                    }

                    @Override
                    public boolean apply(@Nullable T input) {
                        return agent().getSimulationStep() > stepForValue;
                    }
                }
        );
    }


    public Callback<? super SimulationStepProperty<T, A>, T> getCallback() {
        return callback;
    }

    public static <T, A extends Agent<A, ?, ?>> Builder<T, A> builder() {
        return new Builder<T, A>();
    }

    public static class Builder<T, A extends Agent<A, ?, ?>> extends AbstractBuilder<T, A, SimulationStepProperty<T, A>, Builder<T, A>> implements Serializable {

        private Builder() {}

        private Builder(SimulationStepProperty<T, A> simulationStepProperty) {
            super(simulationStepProperty);
        }

        @Override
        protected Builder<T, A> self() {
            return this;
        }

        @Override
        protected SimulationStepProperty<T, A> checkedBuild() {
            return new SimulationStepProperty<T, A>(this);
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

    private abstract static class AbstractBuilder<T, A extends Agent<A, ?, ?>, P extends SimulationStepProperty<T, A>, B extends AbstractBuilder<T, A, P, B>> extends AbstractAgentProperty.AbstractBuilder<P, A, B> implements Serializable {
        private Callback<? super SimulationStepProperty<T, A>, T> callback;

        protected AbstractBuilder() {}

        protected AbstractBuilder(SimulationStepProperty<T, A> simulationStepProperty) {
            super(simulationStepProperty);
            this.callback = simulationStepProperty.callback;
        }

        public B callback(Callback<? super SimulationStepProperty<T, A>, T> function) {
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