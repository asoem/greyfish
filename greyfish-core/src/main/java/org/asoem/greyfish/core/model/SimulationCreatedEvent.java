package org.asoem.greyfish.core.model;

import org.asoem.greyfish.core.simulation.Simulation;

/**
 * An event which is published by {@link org.asoem.greyfish.core.model.Experiment experiments} if they create a new {@link Simulation getSimulation}.
 */
public class SimulationCreatedEvent {
    private final Experiment experiment;
    private final Simulation<?> simulation;

    public SimulationCreatedEvent(final Experiment experiment, final Simulation<?> simulation) {
        this.experiment = experiment;
        this.simulation = simulation;
    }

    /**
     * Get the getSimulation which was created
     * @return the newly created getSimulation
     */
    public Simulation<?> simulation() {
        return simulation;
    }

    /**
     * Get the experiment which created the new {@link #simulation() getSimulation}.
     * @return the event origin
     */
    public Experiment experiment() {
        return experiment;
    }
}
