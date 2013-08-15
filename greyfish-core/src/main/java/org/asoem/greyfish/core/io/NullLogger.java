package org.asoem.greyfish.core.io;

import com.google.inject.Inject;
import org.asoem.greyfish.core.agent.Agent;

import java.io.IOException;

/**
 * A logger that does nothing
 */
public final class NullLogger<A extends Agent<A, ?>> implements SimulationLogger<A> {

    @Inject
    public NullLogger() {
    }

    @Override
    public void logAgentCreation(final A agent) {
    }

    @Override
    public void logAgentEvent(final A agent, final int currentStep, final String source, final String title, final String message) {
    }

    @Override
    public void logProperty(final String marker, final String key, final String value) {
    }

    @Override
    public void close() throws IOException {
    }
}
