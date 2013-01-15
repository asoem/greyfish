package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.utils.space.Point2D;

/**
 * User: christoph
 * Date: 11.12.12
 * Time: 10:21
 */
public interface DefaultGreyfishSimulation extends SpatialSimulation2D<DefaultGreyfishAgent, DefaultGreyfishSpace> {
    /**
     * Create a new Agent of given population
     * @param population the population of the prototype to use for creation
     */
    void createAgent(Population population);

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
}
