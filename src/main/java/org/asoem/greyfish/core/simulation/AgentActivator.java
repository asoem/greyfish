package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 23.11.12
 * Time: 11:36
 */
public interface AgentActivator<A extends Agent<A,?,?>> {
    void activate(A agent);
    void deactivate(A agent);
}
