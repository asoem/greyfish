package org.asoem.greyfish.core.io;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code SimulationLogger} which logs to a JDBC {@link Connection}. This implementation uses a {@link Disruptor} to
 * handle the incoming events and therefore is threadsafe.
 */
final class JDBCLogger<A extends SpatialAgent<A, ? extends BasicSimulationContext<?, A>, ?, ?>>
        implements SimulationLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCLogger.class);
    private static final int RING_BUFFER_SIZE = 1024;

    private final ConnectionManager connectionManager;
    private final Disruptor<BindEvent> disruptor;
    @VisibleForTesting
    final List<BatchQuery> queries;

    private final AtomicInteger nameIdSequence = new AtomicInteger();
    private final Cache<String, Integer> nameIdCache = CacheBuilder.newBuilder()
            .build();

    private final LoadingCache<String, Integer> simulationIdCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Integer>() {
                private final AtomicInteger sequence = new AtomicInteger();

                @Override
                public Integer load(final String key) throws Exception {
                    return sequence.incrementAndGet();
                }
            });

    private Throwable consumerException;

    JDBCLogger(final ConnectionManager connectionManager, final int commitThreshold) {
        checkArgument(commitThreshold > 0);
        this.connectionManager = checkNotNull(connectionManager);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        disruptor = new Disruptor<>(new EventFactory<BindEvent>() {
            @Override
            public BindEvent newInstance() {
                return new BindEvent();
            }
        }, RING_BUFFER_SIZE, executor);

        final EventHandler<BindEvent> eventHandler = new EventHandler<BindEvent>() {
            @Override
            public void onEvent(final BindEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                queries.add(event.updateOperation);

                if (queries.size() == commitThreshold) {
                    flush();
                    queries.clear();
                }
            }
        };

        disruptor.handleExceptionsWith(new ExceptionHandler() { // Attention: Call this method before #handleEventsWith (See javadoc of #handleExceptionsWith)
            @Override
            public void handleEventException(final Throwable ex, final long sequence, final Object event) {
                LOGGER.error("Failed to handle event", ex);
                consumerException = ex;
            }

            @Override
            public void handleOnStartException(final Throwable ex) {
                LOGGER.error("Failed to start", ex);
                consumerException = ex;
            }

            @Override
            public void handleOnShutdownException(final Throwable ex) {
                LOGGER.error("Failed to shutdown", ex);
                consumerException = ex;
            }
        });
        //noinspection unchecked
        disruptor.handleEventsWith(eventHandler);
        try {
            disruptor.start();
        } catch (Throwable t) {
            LoggerFactory.getLogger(JDBCLogger.class).error("Disruptor failed to start", t);
        }
        queries = new ArrayList<BatchQuery>(commitThreshold);
    }

    /**
     * Create sql statements for  all {@code #queries} using a connection from the {@link #connectionManager} and
     * executes them as a batch. The {@link #queries} list is left unmodified.
     *
     * @throws SQLException
     */
    private void executeQueries() throws SQLException {
        final Connection connection = connectionManager.get();

        final LoadingCache<String, PreparedStatement> cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, PreparedStatement>() {
                    @Override
                    public PreparedStatement load(final String key) throws Exception {
                        return connection.prepareStatement(key);
                    }
                });
        try {
            for (BatchQuery query : this.queries) {
                final PreparedStatement preparedStatement = query.prepareStatement(new Function<String, PreparedStatement>() {
                    @Nullable
                    @Override
                    public PreparedStatement apply(@Nullable final String input) {
                        return cache.getUnchecked(input);
                    }
                });
                preparedStatement.addBatch();
            }
            for (PreparedStatement preparedStatement : cache.asMap().values()) {
                try {
                    preparedStatement.executeBatch();
                } catch (SQLException e) {
                    throw Throwables.propagate(e);
                }
            }
            connection.commit();
        } finally {
            for (PreparedStatement preparedStatement : cache.asMap().values()) {
                preparedStatement.close();
            }
            connectionManager.releaseConnection(connection);
        }
    }

    @Override
    public void logSimulation(final Simulation<?> simulation) {
        addQuery(new InsertSimulationQuery(
                simulationIdCache.getUnchecked(simulation.getName()),
                simulation.getName()));
    }

    @Override
    public void logAgentCreation(final int agentId, final String prototypeGroupName, final int activationStep,
                                 final String simulationName, final Set<Integer> parents,
                                 final Map<String, ?> traitValues) {
        addQuery(new InsertAgentQuery(
                agentId,
                idForName(prototypeGroupName),
                activationStep, simulationIdCache.getUnchecked(simulationName)));

        for (final Integer parentId : parents) {
            addQuery(new InsertChromosomeQuery(agentId, parentId));
        }

        for (Map.Entry<String, ?> stringObjectEntry : traitValues.entrySet()) {
            final String traitName = stringObjectEntry.getKey();
            final Object traitValue = stringObjectEntry.getValue();

            final TypeToken<?> valueType = TypeToken.of(traitValue.getClass());

            final Object value = traitValue != null && TypeToken.of(Storeable.class).isAssignableFrom(valueType)
                    ? ((Storeable) traitValue).convert()
                    : traitValue;

            if (value instanceof Number) {
                addQuery(new InsertTraitAsDoubleQuery(
                        agentId,
                        idForName(traitName),
                        Optional.fromNullable((Number) value).or(Double.NaN).doubleValue()));
            } else {
                addQuery(new InsertTraitAsStringQuery(
                        agentId,
                        idForName(traitName),
                        idForName(String.valueOf(value))));
            }
        }
    }

    private void addQuery(final BatchQuery query) {
        if (consumerException != null) {
            throw new IllegalStateException("Consumer thread had errors.", consumerException);
        }
        disruptor.publishEvent(new EventTranslator<BindEvent>() {
            @Override
            public void translateTo(final BindEvent event, final long sequence) {
                event.updateOperation = query;
            }
        });
    }

    @Override
    public void logAgentEvent(final int currentStep, final String source, final String title, final String message, final int agentId, final Object2D projection) {
        final InsertEventQuery insertEventOperation =
                new InsertEventQuery(
                        currentStep, agentId, projection,
                        idForName(source), idForName(title), message);
        addQuery(insertEventOperation);
    }

    @Override
    public void logProperty(final String marker, final String key, final String value) {
        checkNotNull(marker, "marker must not be null");
        checkNotNull(key, "key must not be null");
        checkNotNull(value, "value must not be null");

        final InsertPropertyQuery insertPropertyOperation = new InsertPropertyQuery(marker, key, value);
        addQuery(insertPropertyOperation);
    }

    @Override
    public void close() throws IOException {
        // TODO: Prevent publishing to the ring buffer (see javadoc of Disruptor#shutdown()).
        disruptor.shutdown();
        try {
            flush();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /**
     * Flush this logger. Causes the logger to write all cached (not yet stored stored) logs to the database.
     *
     * @throws SQLException
     */
    public void flush() throws SQLException {
        executeQueries();
    }

    private short idForName(final String name) {
        final Integer id;
        try {
            id = nameIdCache.get(name, new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    final int id = nameIdSequence.incrementAndGet();
                    addQuery(new InsertNameQuery((short) id, name));
                    return id;
                }
            });
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }

        return id.shortValue();
    }

    private interface BatchQuery {

        /**
         * Set the values for the prepared {@code statement}.
         *
         * @param statementFactory the statement factory
         * @throws java.sql.SQLException if an error occurred when setting values
         */
        PreparedStatement prepareStatement(Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException;
    }

    private static final class InsertEventQuery implements BatchQuery {
        private static final String SQL = "INSERT INTO " +
                "agent_events (simulation_step, agent_id, source_name_id, title_name_id, message, x, y) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        private final int currentStep;
        private final int agentId;
        private final Point2D coordinates;
        private final short sourceNameId;
        private final short titleNameId;
        private final String message;

        public InsertEventQuery(final int currentStep, final int agentId,
                                final Object2D coordinate, final short sourceNameId,
                                final short titleNameId, final String message) {
            this.currentStep = currentStep;
            this.agentId = agentId;
            this.coordinates = coordinate.getCentroid();
            this.sourceNameId = sourceNameId;
            this.titleNameId = titleNameId;
            this.message = message;
        }

        @Override
        public PreparedStatement prepareStatement(final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, currentStep);
            preparedStatement.setInt(2, agentId);
            preparedStatement.setShort(3, sourceNameId);
            preparedStatement.setShort(4, titleNameId);
            preparedStatement.setString(5, message);
            preparedStatement.setFloat(6, (float) coordinates.getX());
            preparedStatement.setFloat(7, (float) coordinates.getY());

            return preparedStatement;
        }
    }

    private static final class InsertAgentQuery implements BatchQuery {
        private static final String SQL = "INSERT INTO agents (id, population_name_id, activated_at, simulation_id) VALUES (?, ?, ?, ?)";
        private final int id;
        private final short populationNameId;
        private final int timeOfBirth;
        private final int simulationId;

        public InsertAgentQuery(final int id, final short populationNameId, final int timeOfBirth, final int simulationId) {
            this.id = id;
            this.populationNameId = populationNameId;
            this.timeOfBirth = timeOfBirth;
            this.simulationId = simulationId;
        }

        @Override
        public PreparedStatement prepareStatement(final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, id);
            preparedStatement.setShort(2, populationNameId);
            preparedStatement.setInt(3, timeOfBirth);
            preparedStatement.setInt(4, simulationId);

            return preparedStatement;
        }
    }

    private static final class InsertChromosomeQuery implements BatchQuery {
        private static final String SQL = "INSERT INTO chromosome_tree (child_id, parent_id) VALUES (?, ?)";
        private final int childAgentId;
        private final int parentAgentId;

        public InsertChromosomeQuery(final int childAgentId, final int parentAgentId) {
            this.childAgentId = childAgentId;
            this.parentAgentId = parentAgentId;
        }

        @Override
        public PreparedStatement prepareStatement(final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, childAgentId);
            preparedStatement.setInt(2, parentAgentId);

            return preparedStatement;
        }
    }

    private static final class InsertTraitAsDoubleQuery implements BatchQuery {
        private static final String SQL = "INSERT INTO quantitative_traits (agent_id, trait_name_id, value) VALUES (?, ?, ?)";
        private final int agentId;
        private final short geneNameId;
        private final Double allele;

        public InsertTraitAsDoubleQuery(final int agentId, final short geneNameId, final Double allele) {
            this.agentId = agentId;
            this.geneNameId = geneNameId;
            this.allele = allele;
        }

        @Override
        public PreparedStatement prepareStatement(final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, agentId);
            preparedStatement.setShort(2, geneNameId);
            preparedStatement.setDouble(3, allele);

            return preparedStatement;
        }
    }

    private static final class InsertTraitAsStringQuery implements BatchQuery {
        private static final String SQL = "INSERT INTO discrete_traits (agent_id, trait_name_id, trait_value_id) VALUES (?, ?, ?)";
        private final int agentId;
        private final short geneNameId;
        private final short traitValueId;

        public InsertTraitAsStringQuery(final int agentId, final short geneNameId, final short traitValueId) {
            this.agentId = agentId;
            this.geneNameId = geneNameId;
            this.traitValueId = traitValueId;
        }

        @Override
        public PreparedStatement prepareStatement(final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, agentId);
            preparedStatement.setShort(2, geneNameId);
            preparedStatement.setShort(3, traitValueId);

            return preparedStatement;
        }
    }

    private static final class InsertNameQuery implements BatchQuery {
        private static final String SQL = "INSERT INTO names (id, name) VALUES(?, ?)";
        private final String name;
        private final short id;

        public InsertNameQuery(final short id, final String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public PreparedStatement prepareStatement(final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setShort(1, id);
            preparedStatement.setString(2, name);

            return preparedStatement;
        }
    }

    private static final class InsertPropertyQuery implements BatchQuery {
        private static final String SQL = "INSERT INTO properties (type, key, value) VALUES (?, ?, ?)";
        private final String type;
        private final String key;
        private final String value;

        public InsertPropertyQuery(final String type, final String key, final String value) {
            this.type = type;
            this.key = key;
            this.value = value;
        }

        @Override
        public PreparedStatement prepareStatement(final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setString(1, type);
            preparedStatement.setString(2, key);
            preparedStatement.setString(3, value);

            return preparedStatement;
        }
    }

    private class BindEvent {
        private BatchQuery updateOperation;
    }

    private static class InsertSimulationQuery implements BatchQuery {
        private static final String SQL = "INSERT INTO simulation (id, name) VALUES (?, ?)";

        private final int id;
        private final String name;

        public InsertSimulationQuery(final int id, final String name) {
            this.name = name;
            this.id = id;
        }

        @Override
        public PreparedStatement prepareStatement(final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            final PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, name);

            return preparedStatement;
        }
    }
}
