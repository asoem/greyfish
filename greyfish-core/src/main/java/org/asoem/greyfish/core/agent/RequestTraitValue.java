package org.asoem.greyfish.core.agent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A message to {@link Agent#ask(Object, Class) request} a trait value from an {@link
 * org.asoem.greyfish.core.agent.Agent agent}.
 */
public class RequestTraitValue implements ComponentMessage {
    private final String name;

    public RequestTraitValue(final String name) {
        this.name = checkNotNull(name);
    }

    @Override
    public String componentName() {
        return name;
    }
}
