package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.environment.Environment;

public interface Context<S extends Environment<A>, A extends Agent<?>> {
    /**
     * Get the simulation for this context.
     *
     * @return the simulation
     */
    S getEnvironment();

    Iterable<A> getActiveAgents();

    Iterable<A> getAgents(PrototypeGroup prototypeGroup);
}
