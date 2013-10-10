package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A simulation context which indicates an active state of an agent.
 *
 * @param <S> the type of the simulation
 * @param <A> the type of the agent
 */
public final class DefaultActiveSimulationContext<S extends DiscreteTimeSimulation<A>, A extends Agent<A, S>> implements SimulationContext<S,A> {

    private final S simulation;

    private final long activationStep;

    private final int agentId;

    private DefaultActiveSimulationContext(final S simulation, final int agentId, final long simulationStep) {
        this.simulation = checkNotNull(simulation);
        this.agentId = agentId;
        this.activationStep = simulationStep;
    }

    public static <S extends DiscreteTimeSimulation<A>, A extends Agent<A, S>>
    DefaultActiveSimulationContext<S, A> create(final S simulation, final int agentId, final long simulationStep) {
        return new DefaultActiveSimulationContext<S, A>(simulation, agentId, simulationStep);
    }

    @Override
    public long getActivationStep() {
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
    public long getAge() {
        checkState(getSimulationStep() >= getActivationStep(),
                "Agent seems to be born in the future: activationStep={} > getSimulationStep()={}",
                getActivationStep(), getSimulationStep());

        return getSimulation().getTime() - getActivationStep();
    }

    @Override
    public void logEvent(final A agent, final Object eventOrigin, final String title, final String message) {
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        getSimulation().logAgentEvent(agent, eventOrigin, title, message);
    }

    @Override
    public long getSimulationStep() {
        return getSimulation().getTime();
    }

    private static final long serialVersionUID = 0;
}
