package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.individual.Agent;

import java.util.UUID;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 12:53
 */
public interface SimulationLogger {
    void close();
    void addAgent(Agent agent);
    void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message);
}
