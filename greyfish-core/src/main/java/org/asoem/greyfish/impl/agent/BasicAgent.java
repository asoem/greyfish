package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicContext;
import org.asoem.greyfish.impl.environment.BasicEnvironment;

/**
 * A basic agent configuration to be simulated in a {@link org.asoem.greyfish.impl.environment.BasicEnvironment}.
 */
public interface BasicAgent extends Agent<BasicContext<BasicEnvironment, BasicAgent>> {
}
