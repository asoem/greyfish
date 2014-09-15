package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.impl.simulation.BasicEnvironment;

/**
 * A basic agent configuration to be simulated in a {@link org.asoem.greyfish.impl.simulation.BasicEnvironment}.
 */
public interface BasicAgent extends Agent<BasicSimulationContext<BasicEnvironment, BasicAgent>> {
}
