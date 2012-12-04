package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.utils.base.Initializers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 21.11.12
 * Time: 15:44
 */
public abstract class AbstractSimulation<A extends Agent<A, ?>> implements Simulation<A> {
    @Override
    public int numberOfPopulations() {
        return getPrototypes().size();
    }

    @Override
    public Iterable<A> getAgents(final Population population) {
        checkNotNull(population);

        return Iterables.filter(getAgents(), new Predicate<Agent>() {
            @Override
            public boolean apply(Agent agent) {
                return agent.hasPopulation(population);
            }
        });
    }

    @Override
    public String toString() {
        return "Simulation['" + getName() + "']";
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void logAgentEvent(int agentId, String populationName, double[] coordinates, Object eventOrigin, String title, String message) {
        getSimulationLogger().logAgentEvent(
                getStep(),
                agentId, populationName, coordinates,
                eventOrigin.getClass().getSimpleName(), title, message);
    }

    @Override
    public Iterable<A> filterAgents(Predicate<? super A> predicate) {
        return Iterables.filter(getAgents(), predicate);
    }

    @Override
    public void createAgent(Population population) {
        //final MotionObject2DImpl randomProjection = MotionObject2DImpl.of(RandomUtils.nextDouble(0, getSpace().width()), RandomUtils.nextDouble(0, getSpace().height()));
        createAgent(population, Initializers.emptyInitializer());
    }
}
