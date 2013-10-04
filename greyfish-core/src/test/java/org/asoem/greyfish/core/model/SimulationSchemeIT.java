package org.asoem.greyfish.core.model;

import org.asoem.greyfish.core.simulation.Simulation;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * User: christoph Date: 27.09.13 Time: 18:34
 */
public class SimulationSchemeIT {

    @Test
    public void testIteratedParallelSimulations() throws Exception {
        // given
        new SimulationScheme() {
            @Override
            public void run() {
                ExecutorService executorService = Executors.newCachedThreadPool();

                for (int i = 0; i < 3; i++) {

                    List<Callable<Simulation<?>>> simulationCallables = null;
                    for (int j = 0; i < 10; j++) {
                        final Simulation<?> simulation = null;
                        final Callable<Simulation<?>> simulationCallable = new Callable<Simulation<?>>() {
                            @Override
                            public Simulation<?> call() throws Exception {
                                return simulation;
                            }
                        };
                        simulationCallables.add(simulationCallable);
                    }

                    try {
                        final List<Future<Simulation<?>>> futures = executorService.invokeAll(simulationCallables);

                        // evaluate each simulation

                        // assign a founder probability to each simulation relative to the measured fitness.

                        // n times do: create a new simulation with agents drawn from the a founder simulation,
                        // selected by roulette wheel selection

                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        };

        // when


        // then

    }
}
