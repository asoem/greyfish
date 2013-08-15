package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;

import java.io.IOException;

/**
 * Utility functions for {@link SimulationLogger}s
 */
public final class SimulationLoggers {

    private SimulationLoggers() {
        throw new AssertionError("Not instantiable");
    }

    /**
     * Make {@code logger} thread safe.
     * @param logger the logger to wrap
     * @param <A> the {@link Agent} type of the logger
     * @return a new logger with synchronized methods
     */
    public static <A extends Agent<A, ?>> SimulationLogger<A> synchronizedLogger(final SimulationLogger<A> logger) {
        return new SynchronizedLogger<A>(logger);
    }

    private static final class SynchronizedLogger<A extends Agent<A, ?>> implements SimulationLogger<A> {
        private final SimulationLogger<A> logger;

        public SynchronizedLogger(final SimulationLogger<A> logger) {

            this.logger = logger;
        }

        @Override
        public void logAgentCreation(final A agent) {
            synchronized (this) {
                logger.logAgentCreation(agent);
            }
        }

        @Override
        public void logAgentEvent(final A agent, final int currentStep, final String source,
                                  final String title, final String message) {
            synchronized (this) {
                logger.logAgentEvent(agent, currentStep, source, title, message);
            }
        }

        @Override
        public void logProperty(final String marker, final String key, final String value) {
            synchronized (this) {
                logger.logProperty(marker, key, value);
            }
        }

        @Override
        public void close() throws IOException {
            synchronized (this) {
                logger.close();
            }
        }
    }
}
