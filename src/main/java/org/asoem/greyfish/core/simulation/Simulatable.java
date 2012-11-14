package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 12:54
 */
public interface Simulatable<S extends Simulation<S, A, Z, P>, A extends Agent<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> {
    S simulation();
    void activate(SimulationContext<S, A, Z, P> context);
    void execute();
    void shutDown(SimulationContext<S, A, Z, P> context);
}
