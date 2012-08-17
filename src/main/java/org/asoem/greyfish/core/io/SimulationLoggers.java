package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.individual.Agent;

import java.util.UUID;

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
        public void close() {
            synchronized (this) {
                logger.close();
            }
        }

        @Override
        public void addAgent(Agent agent) {
            synchronized (this) {
                logger.addAgent(agent);
            }
        }

        @Override
        public void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
            synchronized (this) {
                logger.addEvent(eventId, uuid, currentStep, agentId, populationName, coordinates, source, title, message);
            }
        }
    }
}
