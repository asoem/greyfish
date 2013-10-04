package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

/**
 * A simulation context that defines an agent as being passive.
 */
public abstract class PassiveSimulationContext<S extends DiscreteTimeSimulation<A>, A extends Agent<A, S>>
        implements SimulationContext<S, A> {

    protected PassiveSimulationContext() {
    }

    @Override
    public final boolean isActiveContext() {
        return false;
    }
}
