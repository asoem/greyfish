package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 14:56
 */
public interface Model<S extends Simulation<S, A, Z, P>, A extends Agent<S, A, P>, Z extends Space2D<A, P>, P extends Object2D> {
    S createSimulation();
}
