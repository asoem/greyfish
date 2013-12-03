package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.core.simulation.Simulation;

import java.io.IOException;
import java.io.PrintStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A logger that logs all messages to {@link System#out}.
 */
final class ConsoleLogger<A extends Agent<A, SimulationContext<?>>> implements SimulationLogger<A> {

    private final PrintStream printStream;

    public ConsoleLogger(final PrintStream printStream) {
        this.printStream = checkNotNull(printStream);
    }

    @Override
    public void logSimulation(final Simulation<?> simulation) {
        printStream.println("Created getSimulation: " + simulation.getName() + " (" + simulation.hashCode() + ")");
    }

    @Override
    public void logAgentCreation(final A agent) {
        final String message = String.format("Created Agent: %s", agent);
        printStream.println(message);
    }

    @Override
    public void logAgentEvent(final A agent, final long currentStep, final String source,
                              final String title, final String message) {
        final String logLine = String.format("Event: %d\t%s\t%s\t%s\t%s\t%s",
                agent.getContext().get().getSimulation().hashCode(),
                currentStep, agent.getPrototypeGroup(),
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
