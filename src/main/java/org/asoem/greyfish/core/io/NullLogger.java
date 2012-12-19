package org.asoem.greyfish.core.io;

import com.google.inject.Inject;
import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 08.05.12
 * Time: 11:01
 */
public class NullLogger<A extends Agent<A, ?>> implements SimulationLogger<A> {

    @Inject
    public NullLogger() {
    }

    @Override
    public void logAgentCreation(A agent) {
    }

    @Override
    public void logAgentEvent(A agent, int currentStep, String source, String title, String message) {
    }
}
