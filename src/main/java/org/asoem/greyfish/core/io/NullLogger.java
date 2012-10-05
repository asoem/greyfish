package org.asoem.greyfish.core.io;

import com.google.inject.Inject;
import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 08.05.12
 * Time: 11:01
 */
public class NullLogger implements SimulationLogger {

    @Inject
    public NullLogger() {
    }

    @Override
    public void logAgentCreation(Agent agent) {
    }

    @Override
    public void logAgentEvent(int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
    }
}
