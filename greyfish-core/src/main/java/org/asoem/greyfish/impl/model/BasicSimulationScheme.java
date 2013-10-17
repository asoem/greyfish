package org.asoem.greyfish.impl.model;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.model.SimulationScheme;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.impl.simulation.BasicSimulation;
import org.asoem.greyfish.impl.simulation.DefaultBasicSimulation;
import org.asoem.greyfish.utils.math.RandomGenerators;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class BasicSimulationScheme implements SimulationScheme {

    @Override
    public void run() {
        int nRuns = 2;

        List<BasicAgent> initialAgents = Lists.newArrayList();
        for (int i = 0; i < nRuns; i++) {
            // create new simulation with predefined set of agents
            final BasicSimulation simulation = createSimulation(i);
            initializeSimulation(simulation, initialAgents);

            // run the simulation
            runSimulation(simulation, Predicates.alwaysFalse());

            // sample agents for new simulation
            initialAgents.clear();
            final Iterable<BasicAgent> sampledAgents =
                    RandomGenerators.sample(RandomGenerators.rng(), ImmutableList.copyOf(simulation.getAgents()), 30);
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
            simulation.enqueueAddition(agent);
        }
    }

    private BasicSimulation createSimulation(final int id) {
        return DefaultBasicSimulation.builder(String.format("%s#%d", this.getClass().getName(), id)).build();
    }

    public static void main(String[] args) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Future<?> submit = executorService.submit(new BasicSimulationScheme());
        try {
            submit.get();
        } catch (Throwable t) {
            t.printStackTrace();
            executorService.shutdownNow();
            System.exit(1);
        }
    }
}
