package org.asoem.greyfish.core.model;

import org.asoem.greyfish.core.environment.Environment;

/**
 * An event which is published by {@link org.asoem.greyfish.core.model.Experiment experiments} if they create a new
 * {@link org.asoem.greyfish.core.environment.Environment getSimulation}.
 */
public class SimulationCreatedEvent {
    private final Experiment experiment;
    private final Environment<?> environment;

    public SimulationCreatedEvent(final Experiment experiment, final Environment<?> environment) {
        this.experiment = experiment;
        this.environment = environment;
    }

    /**
     * Get the getSimulation which was created
     *
     * @return the newly created getSimulation
     */
    public Environment<?> simulation() {
        return environment;
    }

    /**
     * Get the experiment which created the new {@link #simulation() getSimulation}.
     *
     * @return the event origin
     */
    public Experiment experiment() {
        return experiment;
    }
}
