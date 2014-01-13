package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.agent.ComponentMessage;

public final class TraitMutateValueRequest implements ComponentMessage {
    private final String name;
    private final Object value;

    public TraitMutateValueRequest(final String name, final Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String componentName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
