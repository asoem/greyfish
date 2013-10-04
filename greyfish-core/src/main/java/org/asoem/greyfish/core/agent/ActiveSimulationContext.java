package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

import java.io.Serializable;

/**
 * A simulation context that defines an agent to be active.
 */
public abstract class ActiveSimulationContext<S extends DiscreteTimeSimulation<A>, A extends Agent<A, S>>
        implements SimulationContext<S, A>, Serializable {
    @Override
    public final boolean isActiveContext() {
        return true;
    }
}
