package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;

import java.io.Closeable;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 12:53
 */
public interface SimulationLogger<A extends Agent<A, ?>> extends Closeable {
    void logAgentCreation(A agent);
    void logAgentEvent(A agent, int currentStep, String source, String title, String message);
}
