package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.Simulation;

import java.io.IOException;

/**
 * A logger with empty methods.
 */
enum NullLogger implements SimulationLogger<Agent<?>> {
    INSTANCE;

    @Override
    public void logSimulation(final Simulation<?> simulation) {
        // empty
    }

    @Override
    public void logAgentCreation(final Agent<?> agent) {
        // empty
    }

    @Override
    public void logAgentEvent(final Agent<?> agent, final long currentStep, final String source, final String title, final String message) {
        // empty
    }

    @Override
    public void logProperty(final String marker, final String key, final String value) {
        // empty
    }

    @Override
    public void close() throws IOException {
        // empty
    }
}
