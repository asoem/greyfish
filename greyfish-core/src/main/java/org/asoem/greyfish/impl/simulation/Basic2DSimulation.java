package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.space.DefaultGreyfishTiled2DSpace;
import org.asoem.greyfish.utils.space.Point2D;

public interface Basic2DSimulation extends SpatialSimulation2D<Basic2DAgent, DefaultGreyfishTiled2DSpace> {

    /**
     * Create a new Agent of given population at the given location
     * @param population the population of the prototype to use for creation
     * @param location the initial location
     */
    void createAgent(Population population, Point2D location);

    /**
     * Create a new Agent of given population at the given location
     * @param population the population of the prototype to use for creation
     * @param location the initial location
     * @param chromosome the chromosome which will be used to overwrite the default chromosome
     */
    void createAgent(Population population, Point2D location, Chromosome chromosome);

    void addAgent(Basic2DAgent agent, Point2D point2D);
}
