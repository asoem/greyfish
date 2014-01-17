package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import org.asoem.greyfish.utils.math.RandomGenerators;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph Date: 15.05.12 Time: 16:13
 */
public final class Callbacks {

    private Callbacks() {
    }

    public static <T> Callback<Object, T> constant(final T returnValue) {
        return new ConstantCallback<T>(returnValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> Callback<T, Void> emptyCallback() {
        return EmptyCallback.instance();
    }

    public static <C, T> T call(final Callback<C, T> callback, final C caller) {
        return callback.apply(caller, ImmutableMap.<String, Object>of());
    }

    public static <C, T> Callback<C, T> forSupplier(final Supplier<T> supplier) {
        return new Callback<C, T>() {
            @Override
            public T apply(final C caller, final Map<String, ?> args) {
                return supplier.get();
            }
        };
    }

    public static Callback<Object, Object> returnArgument(final String key) {
        return new ArgumentCallback(key);
    }

    public static <R> Callback<Object, R> returnArgument(final String x, final Class<R> clazz) {
        return new CastingCallback<Object, R>(clazz, returnArgument(x));
    }

    public static <R> Callback<Object, R> willThrow(final RuntimeException exception) {
        return new ThrowingCallable<R>(exception);
    }

    /**
     * The created Callback iterates over the given values and returns them. If the last element is reached, its value
     * returned for all consecutive calls.
     *
     * @param values the values to iterate over
     * @param <T>    the type of the values
     * @return the given values in order.
     */
    public static <T> Callback<Object, T> iterate(final T... values) {
        return new IteratingCallback<T>(Iterators.forArray(values));
    }

    public static <T> Callback<Object, T> sample(final T e1, final T e2) {
        return new Sample2Callback<T>(e1, e2);
    }

    public static Callback<Object, Boolean> alwaysTrue() {
        return BooleanConstantCallback.TRUE;
    }

    public static Callback<Object, Boolean> alwaysFalse() {
        return BooleanConstantCallback.FALSE;
    }

    public static Callback<Object, Boolean> random() {
        return RandomCallback.INSTANCE;
    }

    private static enum EmptyCallback implements Callback<Object, Void> {
        INSTANCE;

        @Override
        public Void apply(final Object caller, final Map<String, ?> args) {
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

        public ConstantCallback(@Nullable final T returnValue) {
            this.value = returnValue;
        }

        @Override
        public T apply(final Object caller, final Map<String, ?> args) {
            return value;
        }

        @SuppressWarnings({"rawtypes", "RedundantIfStatement"})
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ConstantCallback)) {
                return false;
            }

            final ConstantCallback that = (ConstantCallback) o;

            if (value != null ? !value.equals(that.value) : that.value != null) {
                return false;
            }

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

    private static class ArgumentCallback implements Callback<Object, Object>, Serializable {
        private final String x;

        public ArgumentCallback(final String x) {
            this.x = checkNotNull(x);
        }

        @Override
        public Object apply(final Object caller, final Map<String, ?> args) {
            return args.get(x);
        }

        private static final long serialVersionUID = 0;
    }

    private static class CastingCallback<C, R> implements Callback<C, R>, Serializable {

        private final Class<R> clazz;
        private final Callback<C, ?> delegate;

        private CastingCallback(final Class<R> clazz, final Callback<C, ?> delegate) {
            this.clazz = clazz;
            this.delegate = delegate;
        }

        @Override
        public R apply(final C caller, final Map<String, ?> args) {
            return clazz.cast(delegate.apply(caller, args));
        }
    }

    private static class ThrowingCallable<R> implements Callback<Object, R>, Serializable {
        private final RuntimeException exception;

        public ThrowingCallable(final RuntimeException exception) {
            this.exception = checkNotNull(exception);
        }

        @Override
        public R apply(final Object caller, final Map<String, ?> args) {
            throw exception;
        }

        private static final long serialVersionUID = 0;
    }

    private static class IteratingCallback<T> implements Callback<Object, T> {
        private final Iterator<T> values;
        private T current = null;

        public IteratingCallback(final Iterator<T> values) {
            this.values = values;
        }

        @Override
        public T apply(final Object caller, final Map<String, ?> args) {
            if (values.hasNext()) {
                current = values.next();
            }
            return current;
        }
    }

    private static class Sample2Callback<T> implements Callback<Object, T> {
        private final T e1;
        private final T e2;

        public Sample2Callback(final T e1, final T e2) {
            this.e1 = e1;
            this.e2 = e2;
        }

        @Override
        public T apply(final Object caller, final Map<String, ?> args) {
            return RandomGenerators.sample(RandomGenerators.rng(), e1, e2);
        }
    }

    private enum BooleanConstantCallback implements Callback<Object, Boolean> {
        TRUE(true),
        FALSE(false);

        private final boolean bool;

        BooleanConstantCallback(final boolean bool) {
            this.bool = bool;
        }

        @Override
        public Boolean apply(final Object caller, final Map<String, ?> args) {
            return bool;
        }
    }

    private enum RandomCallback implements Callback<Object, Boolean> {
        INSTANCE;

        @Override
        public Boolean apply(final Object caller, final Map<String, ?> args) {
            return RandomGenerators.rng().nextBoolean();
        }
    }
}
