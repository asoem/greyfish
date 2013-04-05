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
public class GenericProperty<A extends Agent<A, ?>, T> extends AbstractAgentProperty<T, A> {

    private Callback<? super GenericProperty<A, T>, T> callback;

    private Callback<? super GenericProperty<A, T>, Boolean> updateRequest;

    private Supplier<T> value;

    private GenericProperty(GenericProperty<A, T> simulationStepProperty, DeepCloner cloner) {
        super(simulationStepProperty, cloner);
        this.callback = simulationStepProperty.callback;
        this.updateRequest = simulationStepProperty.updateRequest;
    }

    private GenericProperty(AbstractBuilder<T, A, ? extends GenericProperty<A, T>, ? extends Builder<T, A>> builder) {
        super(builder);
        this.callback = builder.callback;
        this.updateRequest = builder.updateRequest;
    }

    @Override
    public T getValue() {
        checkState(value != null, "Property was not initialized");
        return value.get();
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new GenericProperty<A, T>(this, cloner);
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
                        return callback.apply(GenericProperty.this, ArgumentMap.of());
                    }
                },
                new UpdateRequest<T>() {

                    @Override
                    public void done() {
                    }

                    @Override
                    public boolean apply(@Nullable T input) {
                        return updateRequest.apply(GenericProperty.this, ArgumentMap.of());
                    }
                }
        );
    }


    public Callback<? super GenericProperty<A, T>, T> getCallback() {
        return callback;
    }

    public static <T, A extends Agent<A, ?>> Builder<T, A> builder() {
        return new Builder<T, A>();
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
        private Callback<? super GenericProperty<A, T>, T> callback;

        private Callback<? super GenericProperty<A, T>, Boolean> updateRequest;

        protected AbstractBuilder() {}

        protected AbstractBuilder(GenericProperty<A, T> simulationStepProperty) {
            super(simulationStepProperty);
            this.callback = simulationStepProperty.callback;
        }

        public B callback(Callback<? super GenericProperty<A, T>, T> function) {
            this.callback = checkNotNull(function);
            return self();
        }

        public B updateRequest(Callback<? super GenericProperty<A, T>, Boolean> updateRequest) {
            this.updateRequest = checkNotNull(updateRequest);
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