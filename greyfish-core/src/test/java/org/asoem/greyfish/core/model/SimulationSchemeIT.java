package org.asoem.greyfish.core.model;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.io.SimulationLoggers;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.impl.agent.DefaultBasicAgent;
import org.asoem.greyfish.impl.simulation.BasicSimulation;
import org.asoem.greyfish.impl.simulation.DefaultBasicSimulation;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.collect.ImmutableList.copyOf;
import static org.asoem.greyfish.utils.math.RandomGenerators.rng;
import static org.asoem.greyfish.utils.math.RandomGenerators.sample;

/**
 * User: christoph Date: 27.09.13 Time: 18:34
 */
public class SimulationSchemeIT {


    @Test
    public void testIteratedSimulations() throws Exception {
        // given
        final SimulationScheme simulationScheme = new SimulationScheme() {
            @Override
            public void run() {
                int nRuns = 2;

                List<BasicAgent> initialAgents = Lists.newArrayList();
                for (int i = 0; i < 10; i++) {
                     initialAgents.add(createAgent());
                }

                for (int i = 0; i < nRuns; i++) {
                    // create new simulation with predefined set of agents
                    final BasicSimulation simulation = createSimulation(i);
                    initializeSimulation(simulation, initialAgents);

                    // run the simulation
                    runSimulation(simulation, new Predicate<BasicSimulation>() {
                        int steps = 10;
                        @Override
                        public boolean apply(@Nullable final BasicSimulation input) {
                            return steps-- > 0;
                        }
                    });

                    // sample agents for new simulation
                    initialAgents.clear();
                    final Iterable<BasicAgent> sampledAgents =
                            sample(rng(), copyOf(simulation.getAgents()), 30);
                    Iterables.addAll(initialAgents, sampledAgents);
                }
            }

            private void runSimulation(final BasicSimulation simulation, final Predicate<? super BasicSimulation> runWhile) {
                while (runWhile.apply(simulation)) {
                    simulation.nextStep();
                }
            }

            private void initializeSimulation(final BasicSimulation simulation, final Iterable<BasicAgent> agents) {
                for (BasicAgent agent : agents) {
                    agent.initialize();
                    simulation.enqueueAddition(agent);
                }
            }

            private BasicSimulation createSimulation(final int id) {
                return DefaultBasicSimulation.builder(String.format("%s#%d", this.getClass().getName(), id))
                        .logger(SimulationLoggers.<BasicAgent>consoleLogger())
                        .build();
            }

            private BasicAgent createAgent() {
                return DefaultBasicAgent.builder(Population.named("test")).build();
            }
        };

        // when
        final Future<?> future = Executors.newSingleThreadExecutor().submit(simulationScheme);
        Futures.getUnchecked(future);

        // then
    }

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
