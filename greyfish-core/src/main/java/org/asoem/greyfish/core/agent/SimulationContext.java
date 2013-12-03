package org.asoem.greyfish.core.agent;

public interface SimulationContext<S> {
    /**
     * Get the getSimulation for this context.
     *
     * @return the getSimulation
     */
    S getSimulation();
}
