package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Moving;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Projectable;

/**
 * User: christoph
 * Date: 04.12.12
 * Time: 10:32
 */
public interface SpatialAgent<A extends Agent<A, S>, S extends SpatialSimulation<A, ?>, P extends Object2D> extends Agent<A, S>, Moving<Motion2D>, Projectable<P> {

    /**
     * Measure the distance from given agent to the first obstacle in the given direction (in degrees [0, 360))
     *
     * @param agent
     * @param degrees
     * @return
     */
    double distance(A agent, double degrees);

    Iterable<A> findNeighbours(double radius);
}
