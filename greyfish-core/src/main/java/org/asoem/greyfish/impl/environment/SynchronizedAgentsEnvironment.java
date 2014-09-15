package org.asoem.greyfish.impl.environment;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.environment.DiscreteTimeEnvironment;
import org.asoem.greyfish.core.scheduler.DiscreteEventScheduler;

/**
 * A getSimulation which executes it's agents in discrete steps. Before each step all agents are synchronized, to ensure
 * that they all share the same knowledge.
 */
public interface SynchronizedAgentsEnvironment<A extends Agent<?>>
        extends DiscreteTimeEnvironment<A>, DiscreteEventScheduler {
}
