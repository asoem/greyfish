package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 09.05.12
 * Time: 11:29
 */
public class SimulationStepProperty<T> extends AbstractGFProperty<T> {

    private Callback<? super SimulationStepProperty<T>, T> callback;

    private Supplier<T> value;

    public SimulationStepProperty(SimulationStepProperty<T> simulationStepProperty, DeepCloner cloner) {
        super(simulationStepProperty, cloner);
        this.callback = simulationStepProperty.callback;
    }

    public SimulationStepProperty(AbstractBuilder<T, ? extends SimulationStepProperty<T>, ? extends Builder> builder) {
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

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static class Builder<T> extends AbstractBuilder<T, SimulationStepProperty<T>, Builder<T>> {

        @Override
        protected Builder<T> self() {
            return this;
        }

        @Override
        protected SimulationStepProperty<T> checkedBuild() {
            return new SimulationStepProperty<T>(this);
        }
    }

    private abstract static class AbstractBuilder<T, P extends SimulationStepProperty<T>, B extends AbstractBuilder<T, P, B>> extends AbstractGFProperty.AbstractBuilder<P, B> {
        public Callback<? super SimulationStepProperty<T>, T> callback;

        public B callback(Callback<? super SimulationStepProperty<T>, T> function) {
            this.callback = checkNotNull(function);
            return self();
        }
    }
}
