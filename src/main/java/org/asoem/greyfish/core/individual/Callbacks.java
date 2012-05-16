package org.asoem.greyfish.core.individual;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 16:13
 */
public class Callbacks {
    public static <T> Callback<Object, T> constant(final T returnValue) {
        return new ConstantCallback<T>(returnValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> Callback<T, Void> emptyCallback() {
        return (Callback<T, Void>) EmptyCallback.INSTANCE;
    }

    public static <C, T> T call(Callback<C, T> callback, C caller) {
        return callback.apply(caller, ImmutableMap.<String, Object>of());
    }

    private static enum EmptyCallback implements Callback<Object, Void> {
        INSTANCE;

        @Override
        public Void apply(Object caller, Map<String, ?> localVariables) {
            return null;
        }
    }

    private static class ConstantCallback<T> implements Callback<Object, T> {
        @Nullable
        private T value;

        public ConstantCallback(T returnValue) {
            this.value = returnValue;
        }

        @Override
        public T apply(Object caller, Map<String, ?> localVariables) {
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
    }
}
