package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.environment.DiscreteTimeEnvironment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A getSimulation context which indicates an active state of an agent.
 *
 * @param <S> the type of the getSimulation
 * @param <A> the type of the agent
 */
public final class DefaultActiveContext<S extends DiscreteTimeEnvironment<A>, A extends Agent<?>> implements BasicContext<S, A> {

    private final S simulation;

    private final long activationStep;

    private final int agentId;

    private DefaultActiveContext(final S simulation, final int agentId, final long simulationStep) {
        this.simulation = checkNotNull(simulation);
        this.agentId = agentId;
        this.activationStep = simulationStep;
    }

    public static <S extends DiscreteTimeEnvironment<A>, A extends Agent<?>>
    DefaultActiveContext<S, A> create(final S simulation, final int agentId, final long simulationStep) {
        return new DefaultActiveContext<>(simulation, agentId, simulationStep);
    }

    @Override
    public long getActivationStep() {
        return activationStep;
    }

    @Override
    public S getEnvironment() {
        return simulation;
    }

    @Override
    public Iterable<A> getActiveAgents() {
        return simulation.getActiveAgents();
    }

    @Override
    public Iterable<A> getAgents(final PrototypeGroup prototypeGroup) {
        return simulation.getAgents(prototypeGroup);
    }

    @Override
    public long getAge() {
        checkState(getSimulationStep() >= getActivationStep(),
                "Agent seems to be born in the future: activationStep={} > simulation.getTime()={}",
                getActivationStep(), simulation.getTime());

        return getEnvironment().getTime() - getActivationStep();
    }

    @Override
    public long getSimulationStep() {
        return getEnvironment().getTime();
    }

    @Override
    public long getTime() {
        return getEnvironment().getTime();
    }

    @Override
    public String simulationName() {
        return getEnvironment().getName();
    }

    private static final long serialVersionUID = 0;
}