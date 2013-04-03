package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 16.08.12
 * Time: 16:50
 */
public final class SimulationLoggers {

    private SimulationLoggers() {}

    public static <A extends Agent<A, ?>> SimulationLogger<A> synchronizedLogger(SimulationLogger<A> logger) {
        return new SynchronizedLogger<A>(logger);
    }

    private static class SynchronizedLogger<A extends Agent<A, ?>> implements SimulationLogger<A> {
        private final SimulationLogger<A> logger;

        public SynchronizedLogger(SimulationLogger<A> logger) {

            this.logger = logger;
        }

        @Override
        public void logAgentCreation(A agent) {
            synchronized (this) {
                logger.logAgentCreation(agent);
            }
        }

        @Override
        public void logAgentEvent(A agent, int currentStep, String source, String title, String message) {
            synchronized (this) {
                logger.logAgentEvent(agent, currentStep, source, title, message);
            }
        }
    }
}
