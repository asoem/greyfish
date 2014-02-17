package org.asoem.greyfish.utils.base;

import java.util.Map;


public abstract class VoidCallback<T> implements Callback<T, Void> {
    @Override
    public Void apply(final T caller, final Map<String, ?> args) {
        exec(caller, args);
        return null;
    }

    protected abstract void exec(T caller, Map<String, ?> args);
}
