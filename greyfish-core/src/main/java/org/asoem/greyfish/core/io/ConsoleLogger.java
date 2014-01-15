package org.asoem.greyfish.core.io;

import com.google.common.base.Joiner;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.Object2D;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A logger that logs all messages to {@link System#out}.
 */
final class ConsoleLogger implements SimulationLogger {

    private final PrintStream printStream;

    public ConsoleLogger(final PrintStream printStream) {
        this.printStream = checkNotNull(printStream);
    }

    @Override
    public void logSimulation(final Simulation<?> simulation) {
        printStream.println("Created simulation: " + simulation.getName() + " (" + simulation.hashCode() + ")");
    }

    @Override
    public void logAgentCreation(final int agentId, final String prototypeGroupName, final int activationStep,
                                 final String simulationName, final Set<Integer> parents,
                                 final Map<String, ?> traitValues) {
        final String message = String.format("Created Agent: %s#%d(%s) @ %s[%d] with %s",
                prototypeGroupName, agentId, Joiner.on(',').join(parents), simulationName, activationStep,
                Joiner.on(',').withKeyValueSeparator(":").join(traitValues));
        printStream.println(message);
    }

    @Override
    public void logAgentEvent(final int currentStep, final String source, final String title,
                              final String message, final int agentId, final Object2D projection) {
        final String logLine = String.format("Event: %d\t%s\t%s\t%s\t%s",
                agentId,
                currentStep,
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
