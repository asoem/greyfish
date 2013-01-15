package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

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
        return (Callback<T, Void>) EmptyCallback.INSTANCE;
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
        return new ArgumentCallback<R>(x, clazz);
    }

    public static <R> Callback<Object, R> willThrow(RuntimeException exception) {
        return new ThrowingCallable<R>(exception);
    }

    /**
     * The created Callback iterates over the given values and returns them.
     * If the last element is reached, its value returned for all consecutive calls.
     * @param values the values to iterate over
     * @param <T> the type of the values
     * @return the given values in order.
     */
    public static <T> Callback<Object, T> iterate(T ... values) {
        return new IteratingCallback<T>(Iterators.forArray(values));
    }

    private static enum EmptyCallback implements Callback<Object, Void> {
        INSTANCE;

        @Override
        public Void apply(Object caller, Arguments arguments) {
            return null;
        }
    }

    private static class ConstantCallback<T> implements Callback<Object, T>, Serializable {

        @Nullable
        private final T value;

        public ConstantCallback(@Nullable T returnValue) {
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
            this.x = checkNotNull(x);
            this.clazz = checkNotNull(clazz);
        }

        @Override
        public R apply(Object caller, Arguments arguments) {
            return clazz.cast(arguments.get(x));
        }

        private static final long serialVersionUID = 0;
    }

    private static class ThrowingCallable<R> implements Callback<Object, R>, Serializable {
        private final RuntimeException exception;

        public ThrowingCallable(RuntimeException exception) {
            this.exception = checkNotNull(exception);
        }

        @Override
        public R apply(Object caller, Arguments arguments) {
            throw exception;
        }

        private static final long serialVersionUID = 0;
    }

    private static class IteratingCallback<T>  implements Callback<Object, T> {
        private final Iterator<T> values;
        private T current = null;

        public IteratingCallback(Iterator<T> values) {
            this.values = values;
        }

        @Override
        public T apply(Object caller, Arguments arguments) {
            if (values.hasNext())
                current = values.next();
            return current;
        }
    }
}
