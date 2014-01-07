package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.agent.SpatialAgent;
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
     * @param <A>    the {@link Agent} type of the logger
     * @return a new logger with synchronized methods
     */
    public static <A extends Agent<?>> SimulationLogger synchronizedLogger(final SimulationLogger logger) {
        if (logger instanceof SynchronizedLogger) {
            return logger;
        } else {
            return new SynchronizedLogger<A>(logger);
        }
    }

    @SuppressWarnings("unchecked")
    public static SimulationLogger nullLogger() {
        return NullLogger.INSTANCE;
    }

    /**
     * This logger prints all messages to {@link System#out}. Same as calling {@code printStreamLogger(System.out)}.
     *
     * @param <A> the type of the agent
     * @return a new console logger
     */
    public static <A extends Agent<?>> SimulationLogger consoleLogger() {
        return printStreamLogger(System.out);
    }

    /**
     * This logger prints all messages to given {@code printStream}.
     *
     * @param <A> the type of the agent
     * @return a new print stream logger
     */
    public static <A extends Agent<?>> SimulationLogger printStreamLogger(final PrintStream printStream) {
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
    public static <A extends SpatialAgent<A, ? extends BasicSimulationContext<?, A>, ?>>
    SimulationLogger createJDBCLogger(final ConnectionManager connectionManager, final int commitThreshold) {
        return new JDBCLogger(connectionManager, commitThreshold);
    }

    private static final class SynchronizedLogger<A extends Agent<?>> implements SimulationLogger {
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
        public void logAgentCreation(final int agentId, final String prototypeGroupName, final int activationStep, final String simulationName, final Set<Integer> parents, final Map<String, Object> traitValues) {
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
