package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 16.08.12
 * Time: 16:50
 */
public final class SimulationLoggers {

    private SimulationLoggers() {}

    public static SimulationLogger<Agent> synchronizedLogger(SimulationLogger<Agent> logger) {
        return new SynchronizedLogger(logger);
    }

    private static class SynchronizedLogger implements SimulationLogger<Agent> {
        private final SimulationLogger<Agent> logger;

        public SynchronizedLogger(SimulationLogger<Agent> logger) {

            this.logger = logger;
        }

        @Override
        public void logAgentCreation(Agent agent) {
            synchronized (this) {
                logger.logAgentCreation(agent);
            }
        }

        @Override
        public void logAgentEvent(Agent agent, int currentStep, String source, String title, String message) {
            synchronized (this) {
                logger.logAgentEvent(, currentStep, source, title, message);
            }
        }
    }
}
