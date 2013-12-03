package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.simulation.Simulation;

public final class TimeChangedEvent implements SimulationEvent {
    private final Simulation<?> simulation;
    private final long from;
    private final long to;

    public TimeChangedEvent(final Simulation<?> simulation, final long from, final long to) {
        this.simulation = simulation;
        this.from = from;
        this.to = to;
    }

    @Override
    public Simulation<?> getSimulation() {
        return simulation;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }
}
