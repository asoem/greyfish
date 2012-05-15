package org.asoem.greyfish.core.individual;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 16:13
 */
public class Callbacks {
    public static <T> Callback<Object, T> constant(final T returnValue) {
        return new Callback<Object, T>() {
            @Override
            public T apply(Object caller, Map<? super String, ?> localVariables) {
                return returnValue;
            }
        };
    }

    private static enum EmptyCallback implements Callback<Void, Void> {
        INSTANCE;

        @Override
        public Void apply(Void caller, Map<? super String, ?> localVariables) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Callback<T, Void> emptyCallback() {
        return (Callback<T, Void>) EmptyCallback.INSTANCE;
    }

    public static <C, T> T call(Callback<C, T> callback, C caller) {
        return callback.apply(caller, ImmutableMap.of());
    }
}
