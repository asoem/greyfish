package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentInitializers;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.MotionObject2DImpl;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 03.10.12
 * Time: 22:08
 */
public abstract class AbstractSimulation implements Simulation {

    @Override
    public int numberOfPopulations() {
        return getPrototypes().size();
    }

    @Override
    public Iterable<Agent> findNeighbours(Agent agent, double radius) {
        return getSpace().getVisibleNeighbours(agent, radius);
    }

    @Override
    public Iterable<Agent> getAgents(final Population population) {
        checkNotNull(population);

        return Iterables.filter(getAgents(), new Predicate<Agent>() {
            @Override
            public boolean apply(Agent agent) {
                return agent.hasPopulation(population);
            }
        });
    }

    @Override
    public List<Agent> getAgents() {
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
        final MotionObject2DImpl randomProjection = MotionObject2DImpl.of(RandomUtils.nextDouble(0, getSpace().getWidth()), RandomUtils.nextDouble(0, getSpace().getHeight()));
        createAgent(population, AgentInitializers.projection(randomProjection));
    }
}
