package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Attribute;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActiveSimulationContext<S extends SpatialSimulation<A, ?>, A extends Agent<A, S, ?>> implements SimulationContext<S,A>, Serializable {

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

    public static <S extends SpatialSimulation<A, ?>, A extends Agent<A, S, ?>> ActiveSimulationContext<S, A> create(S simulation, int agentId, int simulationStep) {
        return new ActiveSimulationContext<S, A>(simulation, agentId, simulationStep);
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