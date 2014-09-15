package org.asoem.greyfish.core.environment;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.utils.DiscreteTime;

/**
 * A Simulation which has discrete time steps.
 */
public interface DiscreteTimeEnvironment<A extends Agent<?>> extends Environment<A>, DiscreteTime {

}
