package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
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
public class SimulationStepProperty<T> extends AbstractAgentProperty<T> {

    private Callback<? super SimulationStepProperty<T>, T> callback;

    private Supplier<T> value;

    private SimulationStepProperty(SimulationStepProperty<T> simulationStepProperty, DeepCloner cloner) {
        super(simulationStepProperty, cloner);
        this.callback = simulationStepProperty.callback;
    }

    private SimulationStepProperty(AbstractBuilder<T, ? extends SimulationStepProperty<T>, ? extends Builder> builder) {
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
        return new SimulationStepProperty<T>(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Value", TypedValueModels.forField("callback", this, new TypeToken<Callback<? super LifetimeProperty<T>, T>>() {
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
                        stepForValue = simulation().getStep();
                    }

                    @Override
                    public boolean apply(@Nullable T input) {
                        return simulation().getStep() > stepForValue;
                    }
                }
        );
    }


    public Callback<? super SimulationStepProperty<T>, T> getCallback() {
        return callback;
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static class Builder<T> extends AbstractBuilder<T, SimulationStepProperty<T>, Builder<T>> implements Serializable {

        private Builder() {}

        private Builder(SimulationStepProperty<T> simulationStepProperty) {
            super(simulationStepProperty);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }

        @Override
        protected SimulationStepProperty<T> checkedBuild() {
            return new SimulationStepProperty<T>(this);
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

    private abstract static class AbstractBuilder<T, P extends SimulationStepProperty<T>, B extends AbstractBuilder<T, P, B>> extends AbstractAgentProperty.AbstractBuilder<P, B> implements Serializable {
        private Callback<? super SimulationStepProperty<T>, T> callback;

        protected AbstractBuilder() {}

        protected AbstractBuilder(SimulationStepProperty<T> simulationStepProperty) {
            super(simulationStepProperty);
            this.callback = simulationStepProperty.callback;
        }

        public B callback(Callback<? super SimulationStepProperty<T>, T> function) {
            this.callback = checkNotNull(function);
            return self();
        }
    }

    private Object writeReplace() {
        return new Builder<T>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }
}