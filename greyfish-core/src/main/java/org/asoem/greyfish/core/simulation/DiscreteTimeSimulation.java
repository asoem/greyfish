package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.utils.DiscreteTime;

/**
 * A Simulation which has discrete time steps.
 */
public interface DiscreteTimeSimulation<A extends Agent<?>> extends Simulation<A>, DiscreteTime {

}
