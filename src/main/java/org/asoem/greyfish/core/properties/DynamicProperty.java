package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 09.05.12
 * Time: 11:29
 */
public class DynamicProperty<T> extends AbstractGFProperty<T> {

    private Callback<? super DynamicProperty<T>, T> callback;

    private final Supplier<T> value = MoreSuppliers.memoize(
            new Supplier<T>() {
                @Override
                public T get() {
                    assert callback != null;
                    return callback.apply(DynamicProperty.this, ArgumentMap.of());
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

    public DynamicProperty(DynamicProperty<T> dynamicProperty, DeepCloner cloner) {
        super(dynamicProperty, cloner);
        this.callback = dynamicProperty.callback;
    }

    public DynamicProperty(AbstractDynamicPropertyBuilder<T, ? extends DynamicProperty<T>, ? extends DynamicPropertyBuilder> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    public T getValue() {
        return value.get();
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DynamicProperty<T>(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Value", TypedValueModels.forField("callback", this, new TypeToken<Callback<? super ConstantProperty<T>, T>>() {
        }));
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public static <T> DynamicPropertyBuilder<T> builder() {
        return new DynamicPropertyBuilder<T>();
    }

    public static class DynamicPropertyBuilder<T> extends AbstractDynamicPropertyBuilder<T, DynamicProperty<T>, DynamicPropertyBuilder<T>> {

        @Override
        protected DynamicPropertyBuilder<T> self() {
            return this;
        }

        @Override
        protected DynamicProperty<T> checkedBuild() {
            return new DynamicProperty<T>(this);
        }
    }

    private abstract static class AbstractDynamicPropertyBuilder<T, P extends DynamicProperty<T>, B extends AbstractDynamicPropertyBuilder<T, P, B>> extends AbstractBuilder<P, B> {
        public Callback<? super DynamicProperty<T>, T> callback;

        public B function(Callback<? super DynamicProperty<T>, T> function) {
            this.callback = checkNotNull(function);
            return self();
        }
    }
}
