package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SimulationContext;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 12:54
 */
public interface Simulatable<S extends DiscreteTimeSimulation<A>, A extends Agent<A, S>> extends Runnable {
    S simulation();
    void activate(SimulationContext<S, A> context);
    @Override
    void run();
    void deactivate();
}
