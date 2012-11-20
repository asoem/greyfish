package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.Initializers;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 03.10.12
 * Time: 22:08
 */
public abstract class AbstractSimulation<S extends Simulation<S, A, Z, P>, A extends Agent<S, A, P>, Z extends Space2D<A, P>, P extends Object2D> implements Simulation<S,A,Z,P> {

    @Override
    public int numberOfPopulations() {
        return getPrototypes().size();
    }

    @Override
    public Iterable<A> findNeighbours(A agent, double distance) {
        return getSpace().getVisibleNeighbours(agent, distance);
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
    public Collection<A> getAgents() {
        return getSpace().getObjects();
    }

    @Override
    public int countAgents() {
        return getSpace().countObjects();
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
    public void createAgent(Population population) {
        //final MotionObject2DImpl randomProjection = MotionObject2DImpl.of(RandomUtils.nextDouble(0, getSpace().width()), RandomUtils.nextDouble(0, getSpace().height()));
        createAgent(population, Initializers.emptyInitializer());
    }
}
