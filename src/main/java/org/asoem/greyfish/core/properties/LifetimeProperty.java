package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.Agent;
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
public class LifetimeProperty<T, A extends Agent<A, ?>> extends AbstractAgentProperty<T,A> {

    private Callback<? super LifetimeProperty<T, A>, T> initializer;

    private Supplier<T> memoizer;

    public LifetimeProperty(LifetimeProperty<T, A> lifetimeProperty, DeepCloner cloner) {
        super(lifetimeProperty, cloner);
        this.initializer = lifetimeProperty.initializer;
    }

    public LifetimeProperty(AbstractBuilder<T, A, ? extends LifetimeProperty<T, A>, ? extends Builder<T,A>> builder) {
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
        e.add("Value", TypedValueModels.forField("callback", this, new TypeToken<Callback<? super LifetimeProperty<T, A>, T>>() {}));
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new LifetimeProperty<T, A>(this, cloner);
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

    public Callback<? super LifetimeProperty<T, A>, T> getInitializer() {
        return initializer;
    }

    public static class Builder<T, A extends Agent<A, ?>> extends AbstractBuilder<T, A, LifetimeProperty<T, A>, Builder<T,A>> implements Serializable {

        public Builder(LifetimeProperty<T, A> lifetimeProperty) {
            super(lifetimeProperty);
        }

        public Builder() {}

        @Override
        protected Builder<T,A> self() {
            return this;
        }

        @Override
        protected LifetimeProperty<T, A> checkedBuild() {
            return new LifetimeProperty<T, A>(this);
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

    private abstract static class AbstractBuilder<T, A extends Agent<A, ?>, P extends LifetimeProperty<T, A>, B extends AbstractBuilder<T, A, P, B>> extends AbstractAgentProperty.AbstractBuilder<P, A, B> implements Serializable {
        private Callback<? super LifetimeProperty<T, A>, T> callback;

        protected AbstractBuilder(LifetimeProperty<T, A> lifetimeProperty) {
            super(lifetimeProperty);
            this.callback = lifetimeProperty.initializer;
        }

        protected AbstractBuilder() {}

        public B initialization(Callback<? super LifetimeProperty<T, A>, T> callback) {
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
