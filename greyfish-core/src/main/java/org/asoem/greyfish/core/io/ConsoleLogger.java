package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;

import java.io.IOException;
import java.io.PrintStream;

/**
 * A logger that logs all messages to {@link System#out}.
 *
 * TODO: extract an abstract stream logger
 */
public final class ConsoleLogger<A extends Agent<A, ?>> implements SimulationLogger<A> {

    private PrintStream printStream = System.out;

    @Override
    public void logAgentCreation(final A agent) {
        final String message = String.format("Created Agent: %s", agent);
        printStream.println(message);
    }

    @Override
    public void logAgentEvent(final A agent, final int currentStep, final String source,
                              final String title, final String message) {
        final String logLine = String.format("%s\t%s\t%s\t%s\t%s",
                currentStep, agent.getPopulation(),
                source, title, message
        );
        printStream.println(logLine);
    }

    @Override
    public void logProperty(final String marker, final String key, final String value) {
        final String message = String.format("%s: %s=%s", marker, key, value);
        printStream.println(message);
    }

    @Override
    public void close() throws IOException {
    }
}
