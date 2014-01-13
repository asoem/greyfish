package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.ComponentMessage;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TraitRecombineRequest<T> implements ComponentMessage {
    private final String name;
    private final T value1;
    private final T value2;

    public TraitRecombineRequest(final String name, final T value1, final T value2) {
        this.name = checkNotNull(name);
        this.value1 = checkNotNull(value1);
        this.value2 = checkNotNull(value2);
    }

    @Override
    public String componentName() {
        return name;
    }

    public T getValue1() {
        return value1;
    }

    public T getValue2() {
        return value2;
    }
}
