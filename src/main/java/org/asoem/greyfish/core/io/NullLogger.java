package org.asoem.greyfish.core.io;

import com.google.inject.Inject;
import org.asoem.greyfish.core.individual.Agent;

import java.util.UUID;

/**
 * User: christoph
 * Date: 08.05.12
 * Time: 11:01
 */
public class NullLogger implements SimulationLogger {

    @Inject
    private NullLogger() {
    }

    @Override
    public void addAgent(Agent agent) {
    }

    @Override
    public void addEvent(UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
    }
}
