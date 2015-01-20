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
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.asoem.greyfish.core.environment.Environment;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
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
@ThreadSafe
public final class BufferedJDBCLogger
        implements JDBCLogger {

    private static final Logger logger = LoggerFactory.getLogger(BufferedJDBCLogger.class);
    private static final int RING_BUFFER_SIZE = 1024;

    private final ConnectionManager connectionManager;
    private final Disruptor<BindEvent> disruptor;
    @VisibleForTesting
    final Queue<StatementProvider> queries;

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

    BufferedJDBCLogger(final ConnectionManager connectionManager, final int commitThreshold) {
        checkArgument(commitThreshold > 0, "commitThreshold must be > 0, was %s", commitThreshold);
        this.connectionManager = checkNotNull(connectionManager);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        disruptor = new Disruptor<>(new EventFactory<BindEvent>() {
            @Override
            public BindEvent newInstance() {
                return new BindEvent();
            }
        }, RING_BUFFER_SIZE, executor, ProducerType.MULTI, new YieldingWaitStrategy());

        final EventHandler<BindEvent> eventHandler = new EventHandler<BindEvent>() {
            @Override
            public void onEvent(final BindEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                queries.add(event.updateOperation);

                if (queries.size() == commitThreshold) {
                    flush();
                }
            }
        };

        // Attention: handleExceptionsWith must be called before handleEventsWith (See javadoc of #handleExceptionsWith)
        disruptor.handleExceptionsWith(new ExceptionHandler() {
            @Override
            public void handleEventException(final Throwable ex, final long sequence, final Object event) {
                logger.error("Failed to handle event", ex);
                consumerException = ex;
            }

            @Override
            public void handleOnStartException(final Throwable ex) {
                logger.error("Failed to start", ex);
                consumerException = ex;
            }

            @Override
            public void handleOnShutdownException(final Throwable ex) {
                logger.error("Failed to shutdown", ex);
                consumerException = ex;
            }
        });
        //noinspection unchecked
        disruptor.handleEventsWith(eventHandler);
        try {
            disruptor.start();
        } catch (Throwable t) {
            LoggerFactory.getLogger(BufferedJDBCLogger.class).error("Disruptor failed to start", t);
        }
        queries = new ArrayDeque<>(commitThreshold);
    }

    /**
     * {@link java.util.Queue#poll() Polls} all {@code #queries}, create sql statements for them using a connection from
     * the {@link #connectionManager} and executes these as a batch query.
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
            while (queries.size() > 0) {
                final StatementProvider query = queries.poll();
                final PreparedStatement preparedStatement = query.prepareStatement(
                        new Function<String, PreparedStatement>() {
                            @Nullable
                            @Override
                            public PreparedStatement apply(@Nullable final String input) {
                                return cache.getUnchecked(input);
                            }
                        }
                );
                preparedStatement.addBatch();
            }

            for (PreparedStatement preparedStatement : cache.asMap().values()) {
                try {
                    preparedStatement.executeBatch();
                } catch (Throwable e) {
                    logger.error("Error while executing statement {}", preparedStatement, e);
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
    public void logEnvironment(final Environment<?> environment) {
        log(new InsertSimulationQuery(
                simulationIdCache.getUnchecked(environment.getName()),
                environment.getName()));
    }

    @Override
    public void logAgentCreation(final int agentId, final String prototypeGroupName, final int activationStep,
                                 final String simulationName, final Set<Integer> parents,
                                 final Map<String, ?> traitValues) {
        log(new InsertAgentQuery(
                agentId,
                idForName(prototypeGroupName),
                activationStep, simulationIdCache.getUnchecked(simulationName)));

        for (final Integer parentId : parents) {
            log(new InsertChromosomeQuery(agentId, parentId));
        }

        for (Map.Entry<String, ?> stringObjectEntry : traitValues.entrySet()) {
            final String traitName = stringObjectEntry.getKey();
            final Object traitValue = stringObjectEntry.getValue();

            final TypeToken<?> valueType = TypeToken.of(traitValue.getClass());

            final Object value = traitValue != null && TypeToken.of(Storeable.class).isAssignableFrom(valueType)
                    ? ((Storeable) traitValue).convert()
                    : traitValue;

            if (value instanceof Number) {
                log(new InsertTraitAsDoubleQuery(
                        agentId,
                        idForName(traitName),
                        Optional.fromNullable((Number) value).or(Double.NaN).doubleValue()));
            } else {
                log(new InsertTraitAsStringQuery(
                        agentId,
                        idForName(traitName),
                        idForName(String.valueOf(value))));
            }
        }
    }

    @Override
    public void log(final StatementProvider query) {
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
    public void logAgentEvent(final int currentStep, final String source, final String title,
                              final String message, final int agentId, final Object2D projection) {
        final InsertEventQuery insertEventOperation =
                new InsertEventQuery(
                        currentStep, agentId, projection,
                        idForName(source), idForName(title), message);
        log(insertEventOperation);
    }

    @Override
    public void logAgentInteraction(final int sourceAgentId, final int targetAgentId,
                                    final String type, final int simulationStep) {
        final InsertInteractionQuery interactionQuery =
                new InsertInteractionQuery(sourceAgentId, targetAgentId, idForName(type), simulationStep);
        log(interactionQuery);
    }

    @Override
    public void logProperty(final String marker, final String key, final String value) {
        checkNotNull(marker, "marker must not be null");
        checkNotNull(key, "key must not be null");
        checkNotNull(value, "value must not be null");

        final InsertPropertyQuery insertPropertyOperation = new InsertPropertyQuery(marker, key, value);
        log(insertPropertyOperation);
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

    @Override
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
                    log(new InsertNameQuery((short) id, name));
                    return id;
                }
            });
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }

        return id.shortValue();
    }

    private static final class InsertEventQuery implements StatementProvider {
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
        public PreparedStatement prepareStatement(
                final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
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

    private static final class InsertAgentQuery implements StatementProvider {
        private static final String SQL =
                "INSERT INTO agents (id, population_name_id, activated_at, simulation_id) VALUES (?, ?, ?, ?)";
        private final int id;
        private final short populationNameId;
        private final int timeOfBirth;
        private final int simulationId;

        public InsertAgentQuery(final int id, final short populationNameId,
                                final int timeOfBirth, final int simulationId) {
            this.id = id;
            this.populationNameId = populationNameId;
            this.timeOfBirth = timeOfBirth;
            this.simulationId = simulationId;
        }

        @Override
        public PreparedStatement prepareStatement(
                final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, id);
            preparedStatement.setShort(2, populationNameId);
            preparedStatement.setInt(3, timeOfBirth);
            preparedStatement.setInt(4, simulationId);

            return preparedStatement;
        }
    }

    private static final class InsertChromosomeQuery implements StatementProvider {
        private static final String SQL = "INSERT INTO chromosome_tree (child_id, parent_id) VALUES (?, ?)";
        private final int childAgentId;
        private final int parentAgentId;

        public InsertChromosomeQuery(final int childAgentId, final int parentAgentId) {
            this.childAgentId = childAgentId;
            this.parentAgentId = parentAgentId;
        }

        @Override
        public PreparedStatement prepareStatement(
                final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, childAgentId);
            preparedStatement.setInt(2, parentAgentId);

            return preparedStatement;
        }
    }

    private static final class InsertTraitAsDoubleQuery implements StatementProvider {
        private static final String SQL =
                "INSERT INTO quantitative_traits (agent_id, trait_name_id, value) VALUES (?, ?, ?)";
        private final int agentId;
        private final short geneNameId;
        private final Double allele;

        public InsertTraitAsDoubleQuery(final int agentId, final short geneNameId, final Double allele) {
            this.agentId = agentId;
            this.geneNameId = geneNameId;
            this.allele = allele;
        }

        @Override
        public PreparedStatement prepareStatement(
                final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, agentId);
            preparedStatement.setShort(2, geneNameId);
            preparedStatement.setDouble(3, allele);

            return preparedStatement;
        }
    }

    private static final class InsertTraitAsStringQuery implements StatementProvider {
        private static final String SQL =
                "INSERT INTO discrete_traits (agent_id, trait_name_id, trait_value_id) VALUES (?, ?, ?)";
        private final int agentId;
        private final short geneNameId;
        private final short traitValueId;

        public InsertTraitAsStringQuery(final int agentId, final short geneNameId, final short traitValueId) {
            this.agentId = agentId;
            this.geneNameId = geneNameId;
            this.traitValueId = traitValueId;
        }

        @Override
        public PreparedStatement prepareStatement(
                final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, agentId);
            preparedStatement.setShort(2, geneNameId);
            preparedStatement.setShort(3, traitValueId);

            return preparedStatement;
        }
    }

    private static final class InsertNameQuery implements StatementProvider {
        private static final String SQL = "INSERT INTO names (id, name) VALUES(?, ?)";
        private final String name;
        private final short id;

        public InsertNameQuery(final short id, final String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public PreparedStatement prepareStatement(
                final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setShort(1, id);
            preparedStatement.setString(2, name);

            return preparedStatement;
        }
    }

    private static final class InsertPropertyQuery implements StatementProvider {
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
        public PreparedStatement prepareStatement(
                final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setString(1, type);
            preparedStatement.setString(2, key);
            preparedStatement.setString(3, value);

            return preparedStatement;
        }
    }

    private class BindEvent {
        private StatementProvider updateOperation;
    }

    private static class InsertSimulationQuery implements StatementProvider {
        private static final String SQL = "INSERT INTO simulation (id, name) VALUES (?, ?)";

        private final int id;
        private final String name;

        public InsertSimulationQuery(final int id, final String name) {
            this.name = name;
            this.id = id;
        }

        @Override
        public PreparedStatement prepareStatement(
                final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            final PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, name);

            return preparedStatement;
        }
    }

    private static class InsertInteractionQuery implements StatementProvider {
        private static final String SQL =
                "INSERT INTO agent_interaction (type_name_id, source_id, target_id, simulation_step) VALUES (?, ?, ?, ?)";

        private final int sourceAgentId;
        private final int targetAgentId;
        private final int typeId;
        private final int simulationStep;

        public InsertInteractionQuery(final int sourceAgentId, final int targetAgentId,
                                      final int typeId, final int simulationStep) {
            this.sourceAgentId = sourceAgentId;
            this.targetAgentId = targetAgentId;
            this.typeId = typeId;
            this.simulationStep = simulationStep;
        }

        @Override
        public PreparedStatement prepareStatement(
                final Function<? super String, ? extends PreparedStatement> statementFactory) throws SQLException {
            final PreparedStatement preparedStatement = checkNotNull(statementFactory.apply(SQL));

            preparedStatement.setInt(1, typeId);
            preparedStatement.setInt(2, sourceAgentId);
            preparedStatement.setInt(3, targetAgentId);
            preparedStatement.setInt(4, simulationStep);

            return preparedStatement;
        }
    }
}
