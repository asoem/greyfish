package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.individual.Agent;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 12:53
 */
public interface SimulationLogger {
    void logAgentCreation(Agent agent);
    void logAgentEvent(int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message);
}
