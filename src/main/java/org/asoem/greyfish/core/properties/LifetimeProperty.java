package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.utils.base.ArgumentMap;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A {@code AgentProperty} implementation that can be used to hold a constant value for the lifetime of an {@code Agent}
 */
public class LifetimeProperty<T> extends AbstractAgentProperty<T> {

    private Callback<? super LifetimeProperty<T>, T> callback;

    private Supplier<T> memoizer;

    public LifetimeProperty(LifetimeProperty<T> functionProperty, DeepCloner cloner) {
        super(functionProperty, cloner);
        this.callback = functionProperty.callback;
    }

    public LifetimeProperty(AbstractBuilder<T, ? extends LifetimeProperty<T>, ? extends Builder> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    public T getValue() {
        checkState(memoizer != null, "LifetimeProperty is not initialized");
        return memoizer.get();
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Value", TypedValueModels.forField("callback", this, new TypeToken<Callback<? super LifetimeProperty<T>, T>>() {}));
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new LifetimeProperty<T>(this, cloner);
    }

    @Override
    public void initialize() {
        super.initialize();
        checkState(callback != null, "No Callback was set");
        this.memoizer = Suppliers.memoize(new Supplier<T>() {
            @Override
            public T get() {
                return callback.apply(LifetimeProperty.this, ArgumentMap.of());
            }
        });
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public Callback<? super LifetimeProperty<T>, T> getCallback() {
        return callback;
    }

    public static class Builder<T> extends AbstractBuilder<T, LifetimeProperty<T>, Builder<T>> implements Serializable {

        @Override
        protected Builder<T> self() {
            return this;
        }

        @Override
        protected LifetimeProperty<T> checkedBuild() {
            return new LifetimeProperty<T>(this);
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

    private abstract static class AbstractBuilder<T, P extends LifetimeProperty<T>, B extends AbstractBuilder<T, P, B>> extends AbstractAgentProperty.AbstractBuilder<P, B> implements Serializable {
        public Callback<? super LifetimeProperty<T>, T> callback;

        public B callback(Callback<? super LifetimeProperty<T>, T> callback) {
            this.callback = checkNotNull(callback);
            return self();
        }
    }

    private Object writeReplace() {
        return new Builder<T>()
                .callback(callback)
                .name(getName());
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }
}
