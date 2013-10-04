package org.asoem.greyfish.cli;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This service monitors a given {@link org.asoem.greyfish.core.simulation.DiscreteTimeSimulation} and prints messages to a given {@link OutputStream} every second.
 */
final class SimulationMonitorService extends AbstractScheduledService {

    private final DiscreteTimeSimulation<?> simulation;
    private final PrintWriter writer;
    private final int steps;

    private String lastMessage = "";

    public SimulationMonitorService(final DiscreteTimeSimulation<?> simulation, final OutputStream outputStream, final int steps) {
        checkArgument(steps > 0);
        this.steps = steps;
        this.simulation = checkNotNull(simulation);
        checkNotNull(outputStream);

        final OutputStreamWriter outputStreamWriter;
        try {
            outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        writer = new PrintWriter(outputStreamWriter, true);
    }

    @Override
    protected void runOneIteration() throws Exception {
        final double progress = (double) simulation.getTime() / steps;
        assert progress >= 0 && progress <= 1;
        final String message = String.format("[%s%s] %d%% (%d/%d) (%d active agents)",
                Strings.repeat("#", (int) (progress * 10)),
                Strings.repeat(" ", 10 - (int) (progress * 10)),
                (int) (progress * 100),
                simulation.getTime(),
                steps,
                simulation.countAgents());
        replaceStatusLine(message, false);
    }

    @Override
    protected void shutDown() throws Exception {
        replaceStatusLine("", true);
    }

    private void replaceStatusLine(final String message, final boolean newLine) {
        writer.print("\r" + Strings.repeat(" ", lastMessage.length()));
        writer.print("\r" + message);
        if (newLine) {
            writer.println();
        }
        writer.flush();
        lastMessage = message;
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS);
    }
}
