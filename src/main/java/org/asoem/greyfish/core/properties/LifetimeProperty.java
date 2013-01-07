package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;
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
public class LifetimeProperty<A extends Agent<A, ?>, T> extends AbstractAgentProperty<T,A> {

    private Callback<? super LifetimeProperty<A, T>, T> initializer;

    private Supplier<T> memoizer;

    public LifetimeProperty(LifetimeProperty<A, T> lifetimeProperty, CloneMap cloner) {
        super(lifetimeProperty, cloner);
        this.initializer = lifetimeProperty.initializer;
    }

    public LifetimeProperty(AbstractBuilder<T, A, ?, ?> builder) {
        super(builder);
        this.initializer = builder.callback;
    }

    @Override
    public T getValue() {
        checkState(memoizer != null, "LifetimeProperty is not initialized");
        return memoizer.get();
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Value", TypedValueModels.forField("callback", this, new TypeToken<Callback<? super LifetimeProperty<A, T>, T>>() {}));
    }

    @Override
    public DeepCloneable deepClone(CloneMap cloneMap) {
        return new LifetimeProperty<A, T>(this, cloneMap);
    }

    @Override
    public void initialize() {
        super.initialize();
        checkState(initializer != null, "No Callback was set");
        this.memoizer = Suppliers.memoize(new Supplier<T>() {
            @Override
            public T get() {
                return initializer.apply(LifetimeProperty.this, ArgumentMap.of());
            }
        });
    }

    public static <T, A extends Agent<A, ?>> Builder<T,A> builder() {
        return new Builder<T,A>();
    }

    public Callback<? super LifetimeProperty<A, T>, T> getInitializer() {
        return initializer;
    }

    public static class Builder<T, A extends Agent<A, ?>> extends LifetimeProperty.AbstractBuilder<T, A, LifetimeProperty<A, T>, Builder<T, A>> implements Serializable {

        public Builder(LifetimeProperty<A, T> lifetimeProperty) {
            super(lifetimeProperty);
        }

        public Builder() {}

        @Override
        protected Builder<T,A> self() {
            return this;
        }

        @Override
        protected LifetimeProperty<A, T> checkedBuild() {
            return new LifetimeProperty<A, T>(this);
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

    private abstract static class AbstractBuilder<T, A extends Agent<A, ?>, P extends LifetimeProperty<A, T>, B extends AbstractBuilder<T, A, P, B>> extends AbstractAgentProperty.AbstractBuilder<P, A, B> implements Serializable {
        private Callback<? super LifetimeProperty<A, T>, T> callback;

        protected AbstractBuilder(LifetimeProperty<A, T> lifetimeProperty) {
            super(lifetimeProperty);
            this.callback = lifetimeProperty.initializer;
        }

        protected AbstractBuilder() {}

        public B initialization(Callback<? super LifetimeProperty<A, T>, T> callback) {
            this.callback = checkNotNull(callback);
            return self();
        }
    }

    private Object writeReplace() {
        return new Builder<T,A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }
}
