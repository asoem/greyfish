package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.ComponentMessage;

public class Mutate implements ComponentMessage {
    private final Object value;
    private final String componentName;

    public Mutate(final String componentName, final Object value) {
        this.componentName = componentName;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String componentName() {
        return componentName;
    }
}
