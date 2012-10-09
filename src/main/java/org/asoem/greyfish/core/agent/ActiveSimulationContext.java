package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Attribute;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActiveSimulationContext implements SimulationContext {

    //@Element(name = "simulation")
    private final Simulation simulation;

    @Attribute(name = "activationStep")
    private final int activationStep;

    @Attribute(name = "agentId")
    private final int agentId;

    private ActiveSimulationContext(Simulation simulation, int agentId, int simulationStep) {
        this.simulation = checkNotNull(simulation);
        this.agentId = agentId;
        this.activationStep = simulationStep;
    }

    public static ActiveSimulationContext create(Simulation simulation, int agentId, int simulationStep) {
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
    public Simulation getSimulation() {
        return simulation;
    }

    @Override
    public int getAge() {
        assert simulation.getStep() >= activationStep;
        return simulation.getStep() - activationStep;
    }

    @Override
    public void logEvent(Agent agent, Object eventOrigin, String title, String message) {
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        final Object2D projection = agent.getProjection();
        assert projection != null;

        simulation.logAgentEvent(agentId, agent.getPopulation().getName(), projection.getAnchorPoint().getCoordinates(), eventOrigin, title, message);
    }

    @Override
    public int getSimulationStep() {
        return simulation.getStep();
    }

    @Override
    public boolean isActiveContext() {
        return true;
    }

}