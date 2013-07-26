package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.Simulation;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ActiveSimulationContext<S extends Simulation<A>, A extends Agent<A, S>> implements SimulationContext<S,A>, Serializable {

    //@Element(name = "simulation")
    private final S simulation;

    private final int activationStep;

    private final int agentId;

    private ActiveSimulationContext(final S simulation, final int agentId, final int simulationStep) {
        this.simulation = checkNotNull(simulation);
        this.agentId = agentId;
        this.activationStep = simulationStep;
    }

    public static <S extends Simulation<A>, A extends Agent<A, S>> ActiveSimulationContext<S, A> create(final S simulation, final int agentId, final int simulationStep) {
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
        checkState(getSimulationStep() >= activationStep,
                "Agent seems to be born in the future: activationStep={} > getSimulationStep()={}",
                activationStep, getSimulationStep());

        return simulation.getSteps() - activationStep;
    }

    @Override
    public void logEvent(final A agent, final Object eventOrigin, final String title, final String message) {
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        simulation.logAgentEvent(agent, eventOrigin, title, message);
    }

    @Override
    public int getSimulationStep() {
        return simulation.getSteps();
    }

    @Override
    public boolean isActiveContext() {
        return true;
    }

    private static final long serialVersionUID = 0;
}