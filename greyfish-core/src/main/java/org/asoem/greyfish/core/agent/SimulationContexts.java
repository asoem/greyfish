package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

public class SimulationContexts {
    private SimulationContexts() {
        throw new UnsupportedOperationException("Not instantiable");
    }

    @SuppressWarnings("unchecked")
    public static <S extends DiscreteTimeSimulation<A>, A extends Agent<A, S>> PassiveSimulationContext<S, A> instance() {
        return (PassiveSimulationContext<S, A>) PassiveSimulationContextHolder.PASSIVE_SIMULATION_CONTEXT;
    }

    private static class PassiveSimulationContextHolder {
         static final PassiveSimulationContext<?, ?> PASSIVE_SIMULATION_CONTEXT = new PassiveSimulationContext() {
             @Override
             public long getActivationStep() {
                 throw new UnsupportedOperationException();
             }

             @Override
             public int getAgentId() {
                 throw new UnsupportedOperationException();
             }

             @Override
             public DiscreteTimeSimulation getSimulation() {
                 throw new UnsupportedOperationException();
             }

             @Override
             public long getAge() {
                 throw new UnsupportedOperationException();
             }

             @Override
             public void logEvent(final Agent agent, final Object eventOrigin, final String title, final String message) {
                 throw new UnsupportedOperationException();
             }

             @Override
             public long getSimulationStep() {
                 throw new UnsupportedOperationException();
             }
         };
    }
}
