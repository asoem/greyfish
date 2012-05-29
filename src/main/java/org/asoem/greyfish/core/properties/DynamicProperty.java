package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 09.05.12
 * Time: 11:29
 */
public class DynamicProperty<T> extends AbstractGFProperty<T> {

    private Function<? super DynamicProperty<T>, ? extends T> function;

    public DynamicProperty(DynamicProperty<T> dynamicProperty, DeepCloner cloner) {
        super(dynamicProperty, cloner);
        this.function = dynamicProperty.function;
    }

    public DynamicProperty(AbstractDynamicPropertyBuilder<T, ? extends DynamicProperty<T>, ? extends DynamicPropertyBuilder> builder) {
        super(builder);
        this.function = builder.function;
    }

    @Override
    public T getValue() {
        return function.apply(this);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DynamicProperty<T>(this, cloner);
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

    private abstract static class AbstractDynamicPropertyBuilder<T, P extends DynamicProperty<T>, B extends AbstractDynamicPropertyBuilder<T,P,B>> extends AbstractBuilder<P, B> {
        public Function<? super DynamicProperty<T>, ? extends T> function;

        public B function(Function<? super DynamicProperty<T>, ? extends T> function) {this.function = checkNotNull(function); return self(); }
    }
}
