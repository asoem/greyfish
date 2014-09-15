package org.asoem.greyfish.impl.environment;

import org.asoem.greyfish.core.environment.Environment;

public final class TimeChangedEvent implements EnvironmentModificationEvent {
    private final Environment<?> environment;
    private final long from;
    private final long to;

    public TimeChangedEvent(final Environment<?> environment, final long from, final long to) {
        this.environment = environment;
        this.from = from;
        this.to = to;
    }

    @Override
    public Environment<?> getEnvironment() {
        return environment;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }
}
