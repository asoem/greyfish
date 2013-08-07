package org.asoem.greyfish.cli;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.asoem.greyfish.core.model.SimulationModel;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.simulation.Simulations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * User: christoph
 * Date: 11.07.13
 * Time: 14:38
 */
public class GreyfishSimulationRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreyfishSimulationRunner.class);
    private final Simulation<?> simulation;
    private final List<Predicate<Simulation<?>>> predicateList;

    private State state = State.STARTUP;

    @Inject
    private GreyfishSimulationRunner(final SimulationModel<?> model,
                                     @Named("steps") final int steps,
                                     @Named("quiet") final boolean quiet) {
        predicateList = Lists.newArrayList();

        predicateList.add(new Predicate<Simulation<?>>() {
            @Override
            public boolean apply(final Simulation<?> parallelizedSimulation) {
                return parallelizedSimulation.getSteps() < steps;
            }
        });

        LOGGER.info("Creating simulation for model {}", model.getClass());
        LOGGER.info("Created model {}", model);

        simulation = model.createSimulation();

        if (!quiet) {
            startSimulationMonitor(simulation, steps, System.out);
        }
    }

    private void startSimulationMonitor(final Simulation<?> simulation, final int steps, final OutputStream outputStream) {
        final OutputStreamWriter outputStreamWriter;
        try {
            outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }

        final Closer closer = Closer.create();
        final PrintWriter writer = closer.register(new PrintWriter(outputStreamWriter, true));

        final Runnable simulationMonitorTask = new Runnable() {
            @Override
            public void run() {
                String progressBar = "";

                try {
                    while (state == State.STARTUP)
                        Thread.sleep(10);

                    while (state == State.RUNNING) {
                        //writer.println(simulation.getStep() + " - " + simulation.countAgents());
                        final double progress = (double) simulation.getSteps() / steps;
                        writer.print("\r" + Strings.repeat(" ", progressBar.length()));
                        progressBar = String.format("\r[%s%s] %d%% (%d/%d) (%d active agents)",
                                Strings.repeat("#", (int) (progress * 10)),
                                Strings.repeat(" ", 10 - (int)(progress * 10)),
                                (int)(progress * 100),
                                simulation.getSteps(),
                                steps,
                                simulation.countAgents());
                        writer.print(progressBar);
                        writer.flush();
                        Thread.sleep(1000);
                        writer.print("\r" + Strings.repeat(" ", progressBar.length()));
                        writer.print("\rDone!");
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Simulation polling thread got interrupted");
                    writer.print("\r" + Strings.repeat(" ", progressBar.length()));
                    writer.print("\rInterrupted!");
                } finally {
                    writer.println();
                    try {
                        closer.close();
                    } catch (IOException e) {
                        LOGGER.warn("Closer.close() had errors", e);
                    }
                }
            }
        };

        Executors.newSingleThreadExecutor().execute(simulationMonitorTask);
    }

    @Override
    public void run() {
        LOGGER.info("Starting {}", simulation);

        state = State.RUNNING;
        try {
            Simulations.runWhile(simulation, Predicates.and(predicateList));
        } finally {
            simulation.shutdown();
            state = State.SHUTDOWN;
        }
    }

    private static enum State {
        STARTUP,
        RUNNING,
        SHUTDOWN
    }
}
