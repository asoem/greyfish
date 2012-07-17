package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code GFProperty} implementation that can be used to hold a constant value for the lifetime of an {@code Agent}
 */
public class ConstantProperty<T> extends AbstractGFProperty<T> {

    private Callback<? super ConstantProperty<T>, T> callback;
    private Supplier<T> value;

    public ConstantProperty(ConstantProperty<T> functionProperty, DeepCloner cloner) {
        super(functionProperty, cloner);
        this.callback = functionProperty.callback;
    }

    public ConstantProperty(AbstractFunctionPropertyBuilder<T, ? extends ConstantProperty<T>, ? extends FunctionPropertyBuilder> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    public T getValue() {
        return value.get();
    }

    @Override
    public void initialize() {
        super.initialize();
        // this implements sort of a lazy variable
        // This solves possible dependency issues to components which have not yet been initialized.
        value = new Supplier<T>() {
            @Override
            public T get() {
                value = Suppliers.ofInstance(Callbacks.call(callback, ConstantProperty.this));
                return value.get();
            }
        };
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Value", TypedValueModels.forField("callback", this, new TypeToken<Callback<? super ConstantProperty<T>, T>>() {
        }));
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ConstantProperty<T>(this, cloner);
    }

    public static <T> FunctionPropertyBuilder<T> builder() {
        return new FunctionPropertyBuilder<T>();
    }

    public static class FunctionPropertyBuilder<T> extends AbstractFunctionPropertyBuilder<T, ConstantProperty<T>, FunctionPropertyBuilder<T>> {

        @Override
        protected FunctionPropertyBuilder<T> self() {
            return this;
        }

        @Override
        protected ConstantProperty<T> checkedBuild() {
            return new ConstantProperty<T>(this);
        }
    }

    private abstract static class AbstractFunctionPropertyBuilder<T, P extends ConstantProperty<T>, B extends AbstractFunctionPropertyBuilder<T, P, B>> extends AbstractBuilder<P, B> {
        public Callback<? super ConstantProperty<T>, T> callback;

        public B callback(Callback<? super ConstantProperty<T>, T> callback) {
            this.callback = checkNotNull(callback);
            return self();
        }
    }
}
