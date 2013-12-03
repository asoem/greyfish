package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Moving;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Projectable;

/**
 * An agent for spatial simulations.
 */
public interface SpatialAgent<A extends SpatialAgent<A, P, C>, P extends Object2D, C extends SimulationContext<? extends SpatialSimulation2D<A, ?>>>
        extends Agent<A, C>, Moving<Motion2D>, Projectable<P> {

    /**
     * Measure the distance from this agent to the first obstacle in the given direction (in {@code degrees} [0, 360))
     *
     * @param degrees the angle in which to measure the distance
     * @return the distance to the first obstacle
     * @see SpatialSimulation2D#distance(SpatialAgent, double)
     */
    double distance(double degrees);

    /**
     * Find all agent which are located around this agent inside given {@code radius}.
     *
     * @param radius the radius of the search
     * @return all agents with distance smaller or equal to {@code radius} to this agent
     */
    Iterable<A> findNeighbours(double radius);
}
