package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;

/**
 * User: christoph
 * Date: 14.02.11
 * Time: 09:59
 */
public interface MovementPattern {

    void apply(Agent agent, ParallelizedSimulation simulation);
}
