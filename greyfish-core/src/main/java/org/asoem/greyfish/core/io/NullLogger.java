package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.Object2D;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * A logger with empty methods.
 */
enum NullLogger implements SimulationLogger {
    INSTANCE;

    @Override
    public void logSimulation(final Simulation<?> simulation) {
        // empty
    }

    @Override
    public void logAgentCreation(final int agentId, final String prototypeGroupName, final int activationStep, final String simulationName, final Set<Integer> parents, final Map<String, Object> traitValues) {
        // empty
    }

    @Override
    public void logAgentEvent(final int currentStep, final String source, final String title, final String message, final int agentId, final Object2D projection) {
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
