package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import org.asoem.greyfish.utils.math.RandomUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 16:13
 */
public final class Callbacks {

    private Callbacks() {}

    public static <T> Callback<Object, T> constant(final T returnValue) {
        return new ConstantCallback<T>(returnValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> Callback<T, Void> emptyCallback() {
        return EmptyCallback.instance();
    }

    public static <C, T> T call(Callback<C, T> callback, C caller) {
        return callback.apply(caller, ImmutableMap.<String, Object>of());
    }

    public static <C, T> Callback<C, T> forSupplier(final Supplier<T> supplier) {
        return new Callback<C, T>() {
            @Override
            public T apply(C caller, Map<String, ?> args) {
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

    public static <T> Callback<Object, T> sample(final T e1, final T e2) {
        return new Sample2Callback<T>(e1, e2);
    }

    private static enum EmptyCallback implements Callback<Object, Void> {
        INSTANCE;

        @Override
        public Void apply(Object caller, Map<String, ?> args) {
            return null;
        }

        @SuppressWarnings("unchecked")
        public static <T> Callback<T, Void> instance() {
            return (Callback<T, Void>) INSTANCE;
        }
    }

    private static class ConstantCallback<T> implements Callback<Object, T>, Serializable {

        @Nullable
        private final T value;

        public ConstantCallback(@Nullable T returnValue) {
            this.value = returnValue;
        }

        @Override
        public T apply(Object caller, Map<String, ?> args) {
            return value;
        }

        @SuppressWarnings({"rawtypes", "RedundantIfStatement"})
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
        public R apply(Object caller, Map<String, ?> args) {
            return clazz.cast(args.get(x));
        }

        private static final long serialVersionUID = 0;
    }

    private static class ThrowingCallable<R> implements Callback<Object, R>, Serializable {
        private final RuntimeException exception;

        public ThrowingCallable(RuntimeException exception) {
            this.exception = checkNotNull(exception);
        }

        @Override
        public R apply(Object caller, Map<String, ?> args) {
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
        public T apply(Object caller, Map<String, ?> args) {
            if (values.hasNext())
                current = values.next();
            return current;
        }
    }

    private static class Sample2Callback<T> implements Callback<Object, T> {
        private final T e1;
        private final T e2;

        public Sample2Callback(T e1, T e2) {
            this.e1 = e1;
            this.e2 = e2;
        }

        @Override
        public T apply(Object caller, Map<String, ?> args) {
            return RandomUtils.sample(e1, e2);
        }
    }
}
