package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SimulationContext;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 12:54
 */
public interface Simulatable<S extends Simulation<S, A, ?, ?>, A extends Agent<S, A, ?>> {
    S simulation();
    void activate(SimulationContext<S, A> context);
    void execute();
    void shutDown(SimulationContext<S, A> context);
}
