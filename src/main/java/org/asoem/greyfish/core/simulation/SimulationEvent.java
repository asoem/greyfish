package org.asoem.greyfish.core.simulation;

/**
 * User: christoph
 * Date: 11.04.11
 * Time: 10:40
 */
public class SimulationEvent {
    public enum Event {
        STEP,
        START,
        PAUSE,
        STOP
    }

    private final Simulation source;
    private final Event event;

    public SimulationEvent(Simulation source, Event event) {
        this.source = source;
        this.event = event;
    }

    public Simulation getSource() {
        return source;
    }

    public Event getEvent() {
        return event;
    }
}
