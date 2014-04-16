package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.Object2D;

import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility functions an factories for {@link SimulationLogger}s
 */
public final class SimulationLoggers {

    private SimulationLoggers() {
        throw new AssertionError("Not instantiable");
    }

    /**
     * Make {@code logger} thread safe by synchronizing its methods.
     *
     * @param logger the logger to wrap
     * @return a new logger with synchronized methods
     */
    public static SimulationLogger synchronizedLogger(final SimulationLogger logger) {
        if (logger instanceof SynchronizedLogger) {
            return logger;
        } else {
            return new SynchronizedLogger(logger);
        }
    }

    @SuppressWarnings("unchecked")
    public static SimulationLogger nullLogger() {
        return NullLogger.INSTANCE;
    }

    /**
     * This logger prints all messages to {@link System#out}. Same as calling {@code printStreamLogger(System.out)}.
     *
     * @return a new console logger
     */
    public static SimulationLogger consoleLogger() {
        return printStreamLogger(System.out);
    }

    /**
     * This logger prints all messages to given {@code printStream}.
     *
     * @return a new print stream logger
     */
    public static SimulationLogger printStreamLogger(final PrintStream printStream) {
        return new ConsoleLogger(printStream);
    }

    /**
     * Create a new SimulationLogger which logs to an JDBC connection managed by given {@code connectionManager}. Insert
     * queries are batch processed and only committed after the given {@code commitThreshold} or if the {@link
     * org.asoem.greyfish.core.io.SimulationLogger#close()} is called. This logger is thread safe and not blocking,
     * because it uses a {@link com.lmax.disruptor.dsl.Disruptor} internally to process events.
     *
     * @param connectionManager the connection manager for sending the queries to
     * @param commitThreshold   the threshold after which to commit a batch of queries
     * @return a new JDBC logger
     */
    public static SimulationLogger createJDBCLogger(final ConnectionManager connectionManager, final int commitThreshold) {
        return new JDBCLogger(connectionManager, commitThreshold);
    }

    private static final class SynchronizedLogger implements SimulationLogger {
        @GuardedBy("this")
        private final SimulationLogger logger;

        public SynchronizedLogger(final SimulationLogger logger) {
            this.logger = checkNotNull(logger);
        }

        @Override
        public void logSimulation(final Simulation<?> simulation) {
            synchronized (this) {
                logger.logSimulation(simulation);
            }
        }

        @Override
        public void logAgentCreation(final int agentId, final String prototypeGroupName, final int activationStep, final String simulationName, final Set<Integer> parents, final Map<String, ?> traitValues) {
            synchronized (this) {
                logger.logAgentCreation(agentId, prototypeGroupName, activationStep, simulationName, parents, traitValues);
            }
        }

        @Override
        public void logAgentEvent(final int currentStep, final String source, final String title, final String message, final int agentId, final Object2D projection) {
            synchronized (this) {
                logger.logAgentEvent(currentStep, source, title, message, agentId, projection);
            }
        }

        @Override
        public void logAgentInteraction(final int sourceAgentId, final int targetAgentId, final String type, final int simulationStep) {
            synchronized (this) {
                logger.logAgentInteraction(sourceAgentId, targetAgentId, type, simulationStep);
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
