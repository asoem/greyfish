package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 29.05.12
 * Time: 10:57
 */
public class ConstantProperty<T> extends AbstractGFProperty<T> {

    private Function<? super ConstantProperty<T>, ? extends T> function;
    private Supplier<T> value;

    public ConstantProperty(ConstantProperty<T> functionProperty, DeepCloner cloner) {
        super(functionProperty, cloner);
        this.function = functionProperty.function;
    }

    public ConstantProperty(AbstractFunctionPropertyBuilder<T, ? extends ConstantProperty<T>, ? extends FunctionPropertyBuilder> builder) {
        super(builder);
        this.function = builder.function;
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
                value = Suppliers.ofInstance(function.apply(ConstantProperty.this));
                return value.get();
            }
        };
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

    private abstract static class AbstractFunctionPropertyBuilder<T, P extends ConstantProperty<T>, B extends AbstractFunctionPropertyBuilder<T,P,B>> extends AbstractBuilder<P, B> {
        public Function<? super ConstantProperty<T>, ? extends T> function;

        public B function(Function<? super ConstantProperty<T>, ? extends T> function) {this.function = checkNotNull(function); return self(); }
    }
}
