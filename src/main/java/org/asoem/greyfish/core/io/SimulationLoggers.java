package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 16.08.12
 * Time: 16:50
 */
public class SimulationLoggers {
    public static SimulationLogger synchronizedLogger(SimulationLogger logger) {
        return new SynchronizedLogger(logger);
    }

    private static class SynchronizedLogger implements SimulationLogger {
        private final SimulationLogger logger;

        public SynchronizedLogger(SimulationLogger logger) {

            this.logger = logger;
        }

        @Override
        public void logAgentCreation(Agent agent) {
            synchronized (this) {
                logger.logAgentCreation(agent);
            }
        }

        @Override
        public void logAgentEvent(int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
            synchronized (this) {
                logger.logAgentEvent(currentStep, agentId, populationName, coordinates, source, title, message);
            }
        }
    }
}
