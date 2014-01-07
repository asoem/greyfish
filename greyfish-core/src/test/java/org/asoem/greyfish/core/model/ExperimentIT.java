package org.asoem.greyfish.core.model;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.asoem.greyfish.core.actions.AbstractAgentAction;
import org.asoem.greyfish.core.actions.ActionExecutionResult;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.core.agent.RequestAllTraitValues;
import org.asoem.greyfish.core.traits.AbstractTrait;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.impl.agent.BasicAgentContext;
import org.asoem.greyfish.impl.agent.DefaultBasicAgent;
import org.asoem.greyfish.impl.simulation.BasicSimulation;
import org.asoem.greyfish.impl.simulation.DefaultBasicSimulation;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.collect.ImmutableList.copyOf;
import static org.asoem.greyfish.utils.math.RandomGenerators.rng;
import static org.asoem.greyfish.utils.math.RandomGenerators.sample;

public class ExperimentIT {

    @Test
    public void testIteratedSimulations() throws Exception {
        // given
        final Experiment experiment = new Experiment() {
            @Override
            public void run() {
                int nRuns = 2;

                List<BasicAgent> initialAgents = Lists.newArrayList();
                for (int i = 0; i < 10; i++) {
                    initialAgents.add(createAgent());
                }

                for (int i = 0; i < nRuns; i++) {
                    // create new getSimulation with predefined set of agents
                    final BasicSimulation simulation = createSimulation(i);
                    initializeSimulation(simulation, initialAgents);

                    // run the getSimulation
                    runSimulation(simulation, new Predicate<BasicSimulation>() {
                        int steps = 10;

                        @Override
                        public boolean apply(@Nullable final BasicSimulation input) {
                            return steps-- > 0;
                        }
                    });

                    // sample agents for new getSimulation
                    initialAgents.clear();
                    final Iterable<BasicAgent> sampledAgents =
                            sample(rng(), copyOf(simulation.getActiveAgents()), 30);
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
                        .build();
            }

            private BasicAgent createAgent() {
                return DefaultBasicAgent.builder(PrototypeGroup.named("test")).build();
            }

            @Override
            public void addSimulationListener(final SimulationListener listener) {
                throw new UnsupportedOperationException("Not implemented");
            }
        };

        // when
        final Future<?> future = Executors.newSingleThreadExecutor().submit(experiment);
        Futures.getUnchecked(future);

        // then
    }

    @Test
    public void testIteratedParallelSimulations() throws Exception {
        // given
        final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));

        // when
        final Experiment experiment = new Experiment() {
            @Override
            public void run() {
                int nRuns = 2;

                List<BasicAgent> initialAgents = Lists.newArrayList();
                for (int i = 0; i < 10; i++) {
                    initialAgents.add(createAgent(ImmutableMap.<String, Object>of()));
                }

                for (int i = 0; i < nRuns; i++) {

                    final int parallelSimulations = 4;
                    final List<ListenableFuture<BasicSimulation>> futures = Lists.newArrayList();
                    for (int j = 0; j < parallelSimulations; j++) {
                        // create new getSimulation with predefined set of agents
                        final BasicSimulation simulation = createSimulation(i);
                        initializeSimulation(simulation, copy(initialAgents));

                        // run the getSimulation
                        Callable<BasicSimulation> runnable = new Callable<BasicSimulation>() {
                            public BasicSimulation call() {
                                runSimulation(simulation, new Predicate<BasicSimulation>() {
                                    int steps = 10;

                                    @Override
                                    public boolean apply(@Nullable final BasicSimulation input) {
                                        return steps-- > 0;
                                    }
                                });
                                return simulation;
                            }
                        };
                        final ListenableFuture<BasicSimulation> future = executorService.submit(runnable);
                        futures.add(future);
                    }

                    final List<BasicSimulation> simulations = Futures.getUnchecked(Futures.allAsList(futures));

                    initialAgents.clear();
                    for (BasicSimulation simulation : simulations) {
                        // sample agents for new getSimulation
                        final Iterable<BasicAgent> sampledAgents =
                                sample(rng(), copyOf(simulation.getActiveAgents()), 30);

                        Iterables.addAll(initialAgents, sampledAgents);
                    }
                }
            }

            private Iterable<BasicAgent> copy(final Iterable<BasicAgent> sampledAgents) {
                return Iterables.transform(sampledAgents, new Function<BasicAgent, BasicAgent>() {
                    @Nullable
                    @Override
                    public BasicAgent apply(@Nullable final BasicAgent input) {
                        Map<String, ?> traitValues = input.ask(new RequestAllTraitValues(), Map.class);
                        final BasicAgent agent = createAgent(traitValues);
                        //HeritableTraitsChromosome.copyFromAgent(input).updateAgent(agent);
                        agent.initialize();
                        return agent;
                    }
                });
            }

            private void runSimulation(final BasicSimulation simulation, final Predicate<? super BasicSimulation> runWhile) {
                while (runWhile.apply(simulation)) {
                    simulation.nextStep();
                }
            }

            private void initializeSimulation(final BasicSimulation simulation, final Iterable<BasicAgent> agents) {
                for (BasicAgent agent : agents) {
                    simulation.enqueueAddition(agent);
                }
            }

            private BasicSimulation createSimulation(final int id) {
                return DefaultBasicSimulation.builder(String.format("%s#%d", this.getClass().getName(), id))
                        .build();
            }

            private BasicAgent createAgent(final Map<String, ?> traitValues) {
                final PrototypeGroup prototypeGroup = PrototypeGroup.named("test");
                return DefaultBasicAgent
                        .builder(prototypeGroup)
                        .addAction(new AbstractAgentAction<BasicAgentContext<BasicAgent>>("action") {
                            @Override
                            public ActionExecutionResult apply(final BasicAgentContext<BasicAgent> context) {
                                final Iterable<BasicAgent> activeAgents = context.getAgents(prototypeGroup);
                                double sum = 0.0;
                                for (Agent<?> activeAgent : activeAgents) {
                                    sum += activeAgent.getPropertyValue("trait", Double.class);
                                }
                                System.out.println(sum);

                                context.addAgent(createAgent(ImmutableMap.of("trait", sum)));
                                context.removeAgent(context.agent());

                                return ActionExecutionResult.BREAK;
                            }
                        })
                        .addProperty(new AbstractTrait<BasicAgent, BasicAgentContext<BasicAgent>, Double>("trait") {

                            private final Double traitValue = Optional.fromNullable((Double) traitValues.get("trait")).or(1.0);

                            @Override
                            public Double value(final BasicAgentContext<BasicAgent> context) {
                                return traitValue;
                            }

                        })
                        .build();
            }

            @Override
            public void addSimulationListener(final SimulationListener listener) {
                throw new UnsupportedOperationException("Not implemented");
            }
        };

        // when
        final Future<?> future = Executors.newSingleThreadExecutor().submit(experiment);
        Futures.getUnchecked(future);

        // then
    }
}
