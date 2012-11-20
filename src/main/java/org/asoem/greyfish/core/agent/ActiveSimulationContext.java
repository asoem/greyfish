package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Attribute;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActiveSimulationContext<S extends Simulation<S, A, Z, P>, A extends Agent<S, A, P>, Z extends Space2D<A, P>, P extends Object2D> implements SimulationContext<S,A>, Serializable {

    //@Element(name = "simulation")
    private final S simulation;

    @Attribute(name = "activationStep")
    private final int activationStep;

    @Attribute(name = "agentId")
    private final int agentId;

    private ActiveSimulationContext(S simulation, int agentId, int simulationStep) {
        this.simulation = checkNotNull(simulation);
        this.agentId = agentId;
        this.activationStep = simulationStep;
    }

    public static <S extends Simulation<S, A, Z, P>, A extends Agent<S, A, P>, Z extends Space2D<A, P>, P extends Object2D> ActiveSimulationContext create(S simulation, int agentId, int simulationStep) {
        return new ActiveSimulationContext(simulation, agentId, simulationStep);
    }

    @Override
    public int getActivationStep() {
        return activationStep;
    }

    @Override
    public int getAgentId() {
        return agentId;
    }

    @Override
    public S getSimulation() {
        return simulation;
    }

    @Override
    public int getAge() {
        assert simulation.getStep() >= activationStep;
        return simulation.getStep() - activationStep;
    }

    @Override
    public void logEvent(A agent, Object eventOrigin, String title, String message) {
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        final Object2D projection = agent.getProjection();
        assert projection != null;

        simulation.logAgentEvent(agentId, agent.getPopulation().getName(), projection.getCentroid().getCoordinate(), eventOrigin, title, message);
    }

    @Override
    public int getSimulationStep() {
        return simulation.getStep();
    }

    @Override
    public boolean isActiveContext() {
        return true;
    }

    private static final long serialVersionUID = 0;
}