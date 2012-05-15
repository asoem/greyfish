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
public class FunctionProperty<T> extends AbstractGFProperty<T> {

    private Function<? super FunctionProperty<T>, ? extends T> function;

    public FunctionProperty(FunctionProperty<T> functionProperty, DeepCloner cloner) {
        super(functionProperty, cloner);
        this.function = functionProperty.function;
    }

    public FunctionProperty(AbstractFunctionPropertyBuilder<T, ? extends FunctionProperty<T>, ? extends FunctionPropertyBuilder> builder) {
        super(builder);
        this.function = builder.function;
    }

    @Override
    public T getValue() {
        return function.apply(this);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new FunctionProperty<T>(this, cloner);
    }

    public static <T> FunctionPropertyBuilder<T> builder() {
        return new FunctionPropertyBuilder<T>();
    }

    public static class FunctionPropertyBuilder<T> extends AbstractFunctionPropertyBuilder<T, FunctionProperty<T>, FunctionPropertyBuilder<T>> {

        @Override
        protected FunctionPropertyBuilder<T> self() {
            return this;
        }

        @Override
        protected FunctionProperty<T> checkedBuild() {
            return new FunctionProperty<T>(this);
        }
    }

    private abstract static class AbstractFunctionPropertyBuilder<T, P extends FunctionProperty<T>, B extends AbstractFunctionPropertyBuilder<T,P,B>> extends AbstractBuilder<P, B> {
        public Function<? super FunctionProperty<T>, ? extends T> function;

        public B function(Function<? super FunctionProperty<T>, ? extends T> function) {this.function = checkNotNull(function); return self(); }
    }
}
