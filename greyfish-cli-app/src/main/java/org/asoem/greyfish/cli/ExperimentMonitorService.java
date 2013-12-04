package org.asoem.greyfish.cli;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.asoem.greyfish.core.model.Experiment;
import org.asoem.greyfish.core.model.SimulationCreatedEvent;
import org.asoem.greyfish.core.simulation.Simulation;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This service monitors a given {@link org.asoem.greyfish.core.simulation.DiscreteTimeSimulation} and prints messages to a given {@link OutputStream} every second.
 */
final class ExperimentMonitorService extends AbstractScheduledService {

    private final Experiment experiment;

    private List<ObservedSimulation> activeSimulations = Lists.newCopyOnWriteArrayList();

    private final PrintWriter writer;
    private final int steps;
    private String lastMessage = "";

    public ExperimentMonitorService(final Experiment experiment, final OutputStream outputStream, final int steps, final EventBus eventBus) {
        checkArgument(steps > 0);
        this.steps = steps;
        this.experiment = checkNotNull(experiment);

        eventBus.register(this);

        checkNotNull(outputStream);

        final OutputStreamWriter outputStreamWriter;
        try {
            outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        writer = new PrintWriter(outputStreamWriter, true);
    }


    @Subscribe
    private void simulationCreatedEventHandler(final SimulationCreatedEvent event) {
        checkNotNull(event);
        activeSimulations.add(new ObservedSimulation(event));
    }

    @Override
    protected void runOneIteration() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (ObservedSimulation event : activeSimulations) {
            final String status = event.simulation().getStatusInfo();
            stringBuilder
                    .append(event.simulation().getName())
                    .append("\t")
                    .append(duration(System.currentTimeMillis() - event.timestamp()))
                    .append("\t")
                    .append(status)
                    .append(System.getProperty("line.separator"));
        }
        replaceStatusLine(stringBuilder.toString(), false);
    }

    private static String duration(final long millis) {
        return String.format("%02d:%02d.%03ds", millis / 60000, (millis % 60000) / 1000, (millis % 1000));
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

    private static class ObservedSimulation {
        private final SimulationCreatedEvent event;

        public ObservedSimulation(final SimulationCreatedEvent event) {
            this.event = event;
        }

        public Simulation<?> simulation() {
            return event.simulation();
        }

        public long timestamp() {
            return System.currentTimeMillis();
        }
    }
}