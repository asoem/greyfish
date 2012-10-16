package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 16:13
 */
public final class Callbacks {

    private Callbacks() {}

    private static final ArgumentMap ZERO_ARGUMENTS = new ArgumentMap(ImmutableMap.<String, Object>of());

    public static <T> Callback<Object, T> constant(final T returnValue) {
        return new ConstantCallback<T>(returnValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> Callback<T, Void> emptyCallback() {
        return (Callback<T, Void>) EmptyCallback.SINGLE_INSTANCE;
    }

    public static <C, T> T call(Callback<C, T> callback, C caller) {
        return callback.apply(caller, ZERO_ARGUMENTS);
    }

    public static <C, T> Callback<C, T> forSupplier(final Supplier<T> supplier) {
        return new Callback<C, T>() {
            @Override
            public T apply(C caller, Arguments arguments) {
                return supplier.get();
            }
        };
    }

    public static <R> Callback<Object, R> returnArgument(String x, Class<R> clazz) {
        return new ArgumentCallback(x, clazz);
    }

    private static enum EmptyCallback implements Callback<Object, Void> {
        SINGLE_INSTANCE;

        @Override
        public Void apply(Object caller, Arguments arguments) {
            return null;
        }
    }

    private static class ConstantCallback<T> implements Callback<Object, T>, Serializable {

        @Element(name = "value", required = false)
        @Nullable
        private final T value;

        public ConstantCallback(@Element(name = "value", required = false) T returnValue) {
            this.value = returnValue;
        }

        @Override
        public T apply(Object caller, Arguments arguments) {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConstantCallback)) return false;

            ConstantCallback that = (ConstantCallback) o;

            if (value != null ? !value.equals(that.value) : that.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "ConstantCallback{" +
                    "value=" + value +
                    '}';
        }

        private static final long serialVersionUID = 0;
    }

    private static class ArgumentCallback<R> implements Callback<Object, R>, Serializable {
        private final String x;
        private final Class<R> clazz;

        public ArgumentCallback(String x, Class<R> clazz) {
            this.x = x;
            this.clazz = clazz;
        }

        @Override
        public R apply(Object caller, Arguments arguments) {
            return clazz.cast(arguments.get(x));
        }
    }
}
