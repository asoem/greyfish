package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.impl.agent.BasicAgent;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractSimulation<A extends Agent<A, ?>> implements DiscreteTimeSimulation<A> {


    /**
     * A standard implementation for {@link #numberOfPopulations()} you can use to implement this method.
     * @return the number of distinct populations of all agents
     */
    protected final int standardNumberOfPopulations() {
        return Sets.newHashSet(Iterables.transform(getAgents(), new Function<A, Population>() {
            @Override
            public Population apply(final A input) {
                return input.getPopulation();
            }
        })).size();
    }

    /**
     * A standard implementation for {@link #filterAgents(com.google.common.base.Predicate)} you can use to implement this method.
     * @param population the population to filter agents for
     * @return a view of {@link #getAgents()} where all agents have {@link org.asoem.greyfish.core.agent.Agent#getPopulation()} equal to {@code population}
     */
    protected final Iterable<A> standardGetAgents(final Population population) {
        checkNotNull(population);

        return Iterables.filter(getAgents(), new Predicate<A>() {
            @Override
            public boolean apply(final A agent) {
                return agent.hasPopulation(population);
            }
        });
    }

    @Override
    public final String toString() {
        return "Simulation['" + getName() + "']";
    }

    @Override
    public void shutdown() {
    }

    @Override
    public final void logAgentEvent(final A agent, final Object eventOrigin, final String title, final String message) {
        getSimulationLogger().logAgentEvent(agent, getTime(), eventOrigin.getClass().getSimpleName(), title, message);
    }

    protected abstract SimulationLogger<? super A> getSimulationLogger();

    @Override
    public final Iterable<A> filterAgents(final Predicate<? super A> predicate) {
        return Iterables.filter(getAgents(), predicate);
    }

    protected int standardCountAgents() {
        return getAgents().size();
    }

    protected int standardCountAgents(final Population population) {
        return Iterables.frequency(getAgents(), new Predicate<BasicAgent>() {
            @Override
            public boolean apply(final BasicAgent input) {
                return population.equals(input.getPopulation());
            }
        });
    }
}
