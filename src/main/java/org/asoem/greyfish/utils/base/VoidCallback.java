package org.asoem.greyfish.utils.base;

import java.util.Map;

/**
 * User: christoph
 * Date: 23.04.13
 * Time: 12:22
 */
public abstract class VoidCallback<T> implements Callback<T, Void> {
    @Override
    public Void apply(T caller, Map<String, ?> args) {
        exec(caller, args);
        return null;
    }

    protected abstract void exec(T caller, Map<String, ?> args);
}
