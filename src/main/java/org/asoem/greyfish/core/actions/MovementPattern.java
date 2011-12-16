package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;

/**
 * User: christoph
 * Date: 14.02.11
 * Time: 09:59
 */
public interface MovementPattern {

    void apply(Agent agent, Simulation simulation);
}
