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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A {@code GFProperty} implementation that can be used to hold a constant value for the lifetime of an {@code Agent}
 */
public class LifetimeProperty<T> extends AbstractGFProperty<T> {

    private Callback<? super LifetimeProperty<T>, T> callback;

    private Supplier<T> value;

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
        checkState(value != null, "LifetimeProperty is not initialized");
        return value.get();
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
        this.value = Suppliers.memoize(new Supplier<T>() {
            @Override
            public T get() {
                return callback.apply(LifetimeProperty.this, ArgumentMap.of());
            }
        });
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static class Builder<T> extends AbstractBuilder<T, LifetimeProperty<T>, Builder<T>> {

        @Override
        protected Builder<T> self() {
            return this;
        }

        @Override
        protected LifetimeProperty<T> checkedBuild() {
            return new LifetimeProperty<T>(this);
        }
    }

    private abstract static class AbstractBuilder<T, P extends LifetimeProperty<T>, B extends AbstractBuilder<T, P, B>> extends AbstractGFProperty.AbstractBuilder<P, B> {
        public Callback<? super LifetimeProperty<T>, T> callback;

        public B callback(Callback<? super LifetimeProperty<T>, T> callback) {
            this.callback = checkNotNull(callback);
            return self();
        }
    }
}
