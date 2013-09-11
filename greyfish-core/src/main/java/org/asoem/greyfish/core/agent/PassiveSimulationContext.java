package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.Simulation;

/**
 * User: christoph
 * Date: 08.03.12
 * Time: 13:08
 */
public enum PassiveSimulationContext implements SimulationContext {

    INSTANCE;

    @Override
    public int getActivationStep() {
        return -1;
    }

    @Override
    public int getAgentId() {
        return -1;
    }

    @Override
    public Simulation<?> getSimulation() {
        return null;
    }

    @Override
    public int getAge() {
        return -1;
    }

    @Override
    public void logEvent(final Agent agent, final Object eventOrigin, final String title, final String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSimulationStep() {
        return -1;
    }

    @Override
    public boolean isActiveContext() {
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <S extends Simulation<A>, A extends Agent<A, S>> SimulationContext<S,A> instance() {
        return (SimulationContext<S, A>) INSTANCE;
    }
}