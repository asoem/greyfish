package org.asoem.greyfish.cli;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.asoem.greyfish.core.model.ModelParameters;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.model.SimulationModel;
import org.asoem.greyfish.core.simulation.Simulations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
            public boolean apply(Simulation<?> parallelizedSimulation) {
                return parallelizedSimulation.getSteps() < steps;
            }
        });

        LOGGER.info("Creating simulation for model {}", model.getClass());
        LOGGER.info("Model parameters after injection: {}",
                Joiner.on(", ").withKeyValueSeparator("=").useForNull("null").join(ModelParameters.extract(model)));

        simulation = model.createSimulation();

        if (!quiet) {
            startSimulationMonitor(simulation, steps, System.out);
        }
    }

    private void startSimulationMonitor(final Simulation<?> simulation, final int steps, OutputStream outputStream) {
        OutputStreamWriter outputStreamWriter;
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
                    }

                    writer.print("\r" + Strings.repeat(" ", progressBar.length()));
                    writer.println("\rDone!");
                } catch (InterruptedException e) {
                    LOGGER.error("Simulation polling thread got interrupted");
                } finally {
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
        final Runnable simulationTask = new Runnable() {
            @Override
            public void run() {
                state = State.RUNNING;
                Simulations.runWhile(simulation, Predicates.and(predicateList));
            }
        };
        final Future<?> future = Executors.newSingleThreadExecutor().submit(simulationTask);
        try {
            future.get();
        } catch (InterruptedException e) {
            LOGGER.error("Simulation thread got interrupted", e);
        } catch (ExecutionException e) {
            LOGGER.error("Exception occurred while executing simulation", e);
        }
        finally {
            LOGGER.info("Shutting down simulation {}", simulation);
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
