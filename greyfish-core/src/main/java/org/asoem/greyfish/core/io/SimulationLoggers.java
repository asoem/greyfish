package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.simulation.SpatialSimulation2D;

import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.io.PrintStream;

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
     * @param <A>    the {@link Agent} type of the logger
     * @return a new logger with synchronized methods
     */
    public static <A extends Agent<A, SimulationContext<?>>> SimulationLogger<A> synchronizedLogger(final SimulationLogger<A> logger) {
        if (logger instanceof SynchronizedLogger) {
            return logger;
        } else {
            return new SynchronizedLogger<A>(logger);
        }
    }

    @SuppressWarnings("unchecked")
    public static SimulationLogger<Agent<?, SimulationContext<?>>> nullLogger() {
        return NullLogger.INSTANCE;
    }

    /**
     * This logger prints all messages to {@link System#out}. Same as calling {@code printStreamLogger(System.out)}.
     *
     * @param <A> the type of the agent
     * @return a new console logger
     */
    public static <A extends Agent<A, SimulationContext<?>>> SimulationLogger<A> consoleLogger() {
        return printStreamLogger(System.out);
    }

    /**
     * This logger prints all messages to given {@code printStream}.
     *
     * @param <A> the type of the agent
     * @return a new print stream logger
     */
    public static <A extends Agent<A, SimulationContext<?>>> SimulationLogger<A> printStreamLogger(final PrintStream printStream) {
        return new ConsoleLogger<A>(printStream);
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
    public static <A extends SpatialAgent<?, ?, ? extends BasicSimulationContext<? extends SpatialSimulation2D<?, ?>, ? extends SpatialAgent<?, ?, ?>>>> SimulationLogger<A> createJDBCLogger(final ConnectionManager connectionManager, final int commitThreshold) {
        return new JDBCLogger<>(connectionManager, commitThreshold);
    }

    private static final class SynchronizedLogger<A extends Agent<A, SimulationContext<?>>> implements SimulationLogger<A> {
        @GuardedBy("this")
        private final SimulationLogger<A> logger;

        public SynchronizedLogger(final SimulationLogger<A> logger) {
            this.logger = checkNotNull(logger);
        }

        @Override
        public void logSimulation(final Simulation<?> simulation) {
            synchronized (this) {
                logger.logSimulation(simulation);
            }
        }

        @Override
        public void logAgentCreation(final A agent) {
            synchronized (this) {
                logger.logAgentCreation(agent);
            }
        }

        @Override
        public void logAgentEvent(final A agent, final long currentStep, final String source,
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
