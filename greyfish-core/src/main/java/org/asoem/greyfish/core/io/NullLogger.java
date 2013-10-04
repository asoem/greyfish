package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;

import java.io.IOException;

/**
 * A logger with empty methods.
 */
enum NullLogger implements SimulationLogger<Agent<?, ?>> {
    INSTANCE;

    @Override
    public void logAgentCreation(final Agent<?, ?> agent) {
    }

    @Override
    public void logAgentEvent(final Agent<?, ?> agent, final long currentStep, final String source, final String title, final String message) {
    }

    @Override
    public void logProperty(final String marker, final String key, final String value) {
    }

    @Override
    public void close() throws IOException {
    }
}
