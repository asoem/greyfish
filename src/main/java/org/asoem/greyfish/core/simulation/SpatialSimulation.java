package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.space.Space2D;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 10:50
 */
public interface SpatialSimulation<A extends Agent<A, ?>, Z extends Space2D<A, ?>> extends Simulation<A> {

    /**
     * Find all neighbours of {@code agent} within the given {@code distance}
     *
     * @param agent The focal {@code agent}
     * @param distance The maximum allowed distance of an agent to count as a neighbour
     * @return all neighbours of {@code agent} within the given distance
     */
    Iterable<A> findNeighbours(A agent, double distance);

    /**
     * Get the space used in this simulation
     * @return the space used in this simulation
     */
    Z getSpace();

    /**
     * Measure the distance from given agent to the first obstacle in the given direction (in degrees [0, 360))
     *
     * @param agent
     * @param degrees
     * @return
     */
    double distance(A agent, double degrees);
}
