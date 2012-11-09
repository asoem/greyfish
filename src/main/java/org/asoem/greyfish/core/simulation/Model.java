package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.space.Space2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 14:56
 */
public interface Model<A extends Agent, S extends Space2D<Agent>> {
    Set<A> createPrototypes();
    S createSpace();
    void initialize(Simulation simulation);
}
