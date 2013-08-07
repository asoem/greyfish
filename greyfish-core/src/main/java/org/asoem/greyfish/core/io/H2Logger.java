package org.asoem.greyfish.core.io;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 23.04.12
 * Time: 14:49
 */
public class H2Logger<A extends SpatialAgent<A, ?, ?>> implements SimulationLogger<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2Logger.class);
    private static final int COMMIT_THRESHOLD = 1000;

    private final Supplier<Connection> connection;
    private final List<UpdateOperation> updateOperationList;
    private final NameIdMap nameIdMap;

    private H2Logger(final String path) {
        this.connection = Suppliers.memoize(new Supplier<Connection>() {
            @Override
            public Connection get() {
                loadDriver();
                checkNotNull(path, "path is null");

                final Connection connection1;
                try {
                    connection1 = createConnection(path);
                    initDatabaseStructure(connection1);
                    connection1.setAutoCommit(false);
                } catch (SQLException e) {
                    throw new IOError(e);
                }
                return connection1;
            }
        });
        nameIdMap = new NameIdMap();
        updateOperationList = new ArrayList<UpdateOperation>(COMMIT_THRESHOLD);
    }

    public static <A extends SpatialAgent<A, ?, ?>> H2Logger<A> create(final String path) {
        return new H2Logger<A>(path);
    }

    private Connection createConnection(String path) throws SQLException {
        path = path.replace("~",System.getProperty("user.home"));
        final File file = new File(path + ".h2.db");
        LOGGER.info("Creating H2 database file at {}", file.getAbsolutePath());
        checkArgument(!file.exists(), "Database file exists: %s", file.getAbsolutePath());
        final String url = String.format("jdbc:h2:%s;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;DB_CLOSE_ON_EXIT=FALSE", path);
        final Connection connection = DriverManager.getConnection(url, "sa", "");
        LOGGER.info("Connection opened to url {}", url);
        assert connection != null;
        return connection;
    }

    private void loadDriver() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("The H2 database driver could not be found", e);
        }
    }

    private void initDatabaseStructure(final Connection connection) throws SQLException {
        assert connection != null;
        final Statement statement = connection.createStatement();
        try {
            statement.execute(
                    "CREATE TABLE agents (" +
                            "id INT NOT NULL PRIMARY KEY," +
                            "population_name_id SMALLINT NOT NULL," +
                            "activated_at INT NOT NULL" +
                            ")");
            statement.execute(
                    "CREATE TABLE chromosome_tree (" +
                            "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                            "child_id INT NOT NULL," +
                            "parent_id INT NOT NULL" +
                            ")");
            statement.execute(
                    "CREATE TABLE names (" +
                            "id SMALLINT NOT NULL PRIMARY KEY," +
                            "name VARCHAR(255) UNIQUE NOT NULL" +
                            ")");
            statement.execute(
                    "CREATE TABLE quantitative_traits (" +
                            "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                            "agent_id INT NOT NULL," +
                            "trait_name_id SMALLINT NOT NULL," +
                            "value REAL NOT NULL" +
                            ")");
            statement.execute(
                    "CREATE TABLE discrete_traits (" +
                            "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                            "agent_id INT NOT NULL, " +
                            "trait_name_id SMALLINT NOT NULL, " +
                            "trait_value_id SMALLINT NOT NULL" +
                            ")");
            statement.execute(
                    "CREATE TABLE agent_events (" +
                            "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                            "simulation_step INT NOT NULL, " +
                            "agent_id INT NOT NULL, " +
                            "source_name_id SMALLINT NOT NULL, " +
                            "title_name_id SMALLINT NOT NULL, " +
                            "message VARCHAR(255) NOT NULL, " +
                            "x REAL NOT NULL, " +
                            "y REAL NOT NULL " +
                            ")");
        }
        catch (SQLException e) {
            try {
                this.connection.get().close();
            } catch (SQLException e1) {
                LOGGER.warn("Exception during connection.close()", e1);
            }
            throw e;
        }
        finally {
            closeStatement(statement);
        }
    }

    private void finalizeAndShutdownDatabase() throws SQLException {
        LOGGER.info("Finalizing and shutting down the database");

        commit();
        connection.get().setAutoCommit(true);

        final Statement statement = connection.get().createStatement();
        statement.execute(
                "CREATE INDEX ON quantitative_traits(agent_id)");
        statement.execute(
                "CREATE INDEX ON quantitative_traits(trait_name_id)");
        statement.execute(
                "CREATE INDEX ON discrete_traits(agent_id)");
        statement.execute(
                "CREATE INDEX ON discrete_traits(trait_name_id)");
        statement.execute(
                "CREATE INDEX ON discrete_traits(trait_value_id)");
        statement.execute(
                "CREATE INDEX ON agent_events(agent_id)");
        statement.execute(
                "CREATE INDEX ON agent_events(title_name_id)");
        statement.execute(
                "CREATE INDEX ON agent_events(simulation_step)");
        statement.execute(
                "CREATE UNIQUE HASH INDEX ON names(name)");
        statement.execute(
                "CREATE INDEX ON chromosome_tree(child_id)");
        statement.execute(
                "CREATE INDEX ON chromosome_tree(parent_id)");
        statement.execute(
                "SHUTDOWN COMPACT"); // This causes all connections to it to get closed

        LOGGER.debug("Shutdown complete");
    }

    private void tryCommit() {
        try {
            if (shouldCommit())
                commit();
        }
        catch (SQLException e) {
            throw new IOError(e);
        }
    }

    private void commit() throws SQLException {
        final Map<String, PreparedStatement> preparedStatementMap = Maps.newHashMap();

        try {
            for (final UpdateOperation updateOperation : updateOperationList) {
                final String sql = updateOperation.sqlString();
                if (!preparedStatementMap.containsKey(sql)) {
                    preparedStatementMap.put(sql, connection.get().prepareStatement(sql));
                }
                final PreparedStatement preparedStatement = preparedStatementMap.get(sql);
                updateOperation.update(preparedStatement);
                preparedStatement.addBatch();
            }
            updateOperationList.clear();

            for (final PreparedStatement preparedStatement : preparedStatementMap.values()) {
                preparedStatement.executeBatch();
            }

            connection.get().commit();
        } finally {
            for (final PreparedStatement preparedStatement : preparedStatementMap.values()) {
                closeStatement(preparedStatement);
            }
        }
    }

    private boolean shouldCommit() {
        return updateOperationList.size() >= COMMIT_THRESHOLD;
    }

    private static void closeStatement(@Nullable final Statement statement) {
        if (statement == null)
            return;
        try{
            if (!statement.isClosed())
                statement.close();
        }
        catch (SQLException e) {
            LOGGER.warn("Exception occurred while closing statement {}", statement, e);
        }
    }

    private void addUpdateOperation(final UpdateOperation updateOperation) {
        updateOperationList.add(updateOperation);
    }

    private short idForName(final String name) {
        if (nameIdMap.contains(name)) {
            return nameIdMap.get(name);
        }
        else {
            final short id = nameIdMap.create(name);
            addUpdateOperation(new InsertNameOperation(id, name));
            return id;
        }
    }

    @Override
    public void logAgentCreation(final A agent) {
        addUpdateOperation(new InsertAgentOperation(
                agent.getId(),
                idForName(agent.getPopulation().getName()),
                agent.getTimeOfBirth()));

        final Set<Integer> parents = agent.getParents();
        for (final Integer parentId : parents) {
            addUpdateOperation(new InsertChromosomeOperation(agent.getId(), parentId));
        }
        for (final AgentTrait<?, ?> trait : agent.getTraits()) {
            assert trait != null;

            final TypeToken<?> valueType = trait.getValueType();
            final Object rawValue = trait.get();
            final Object value = rawValue != null && TypeToken.of(Storeable.class).isAssignableFrom(valueType)
                    ? ((Storeable) rawValue).convert()
                    : rawValue;

            if (value instanceof Number) {
                addUpdateOperation(new InsertTraitAsDoubleOperation(
                        agent.getId(),
                        idForName(trait.getName()),
                        Optional.fromNullable((Number) value).or(Double.NaN).doubleValue()));
            }
            else {
                addUpdateOperation(new InsertTraitAsStringOperation(
                        agent.getId(),
                        idForName(trait.getName()),
                        idForName(String.valueOf(value))));
            }
        }
        tryCommit();
    }

    @Override
    public void logAgentEvent(final A agent, final int currentStep, final String source, final String title, final String message) {
        addUpdateOperation(new InsertEventOperation(currentStep, agent.getId(), agent.getProjection(), idForName(source), idForName(title), message));
        tryCommit();
    }

    @Override
    public void close() throws IOException {
        try {
            finalizeAndShutdownDatabase();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private static interface UpdateOperation {
        public String sqlString();
        public void update(PreparedStatement statement) throws SQLException;
    }

    private static class InsertEventOperation implements UpdateOperation {
        private final int currentStep;
        private final int agentId;
        private final Point2D coordinates;
        private final short sourceNameId;
        private final short titleNameId;
        private final String message;

        public InsertEventOperation(final int currentStep, final int agentId, final Object2D coordinate, final short sourceNameId, final short titleNameId, final String message) {
            this.currentStep = currentStep;
            this.agentId = agentId;
            this.coordinates = coordinate.getCentroid();
            this.sourceNameId = sourceNameId;
            this.titleNameId = titleNameId;
            this.message = message;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO agent_events (simulation_step, agent_id, source_name_id, title_name_id, message, x, y) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }

        @Override
        public void update(final PreparedStatement statement) throws SQLException {
            statement.setInt(1, currentStep);
            statement.setInt(2, agentId);
            statement.setShort(3, sourceNameId);
            statement.setShort(4, titleNameId);
            statement.setString(5, message);
            statement.setFloat(6, (float) coordinates.getX());
            statement.setFloat(7, (float) coordinates.getY());
        }
    }

    private static class InsertAgentOperation implements UpdateOperation {
        private final int id;
        private final short populationNameId;
        private final int timeOfBirth;

        public InsertAgentOperation(final int id, final short populationNameId, final int timeOfBirth) {
            this.id = id;
            this.populationNameId = populationNameId;
            this.timeOfBirth = timeOfBirth;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO agents (id, population_name_id, activated_at) VALUES (?, ?, ?)";
        }

        @Override
        public void update(final PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setShort(2, populationNameId);
            statement.setInt(3, timeOfBirth);
        }
    }

    private static class InsertChromosomeOperation implements UpdateOperation {
        private final int childAgentId;
        private final int parentAgentId;

        public InsertChromosomeOperation(final int childAgentId, final int parentAgentId) {
            this.childAgentId = childAgentId;
            this.parentAgentId = parentAgentId;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO chromosome_tree (child_id, parent_id) VALUES (?, ?)";
        }

        @Override
        public void update(final PreparedStatement statement) throws SQLException {
            statement.setInt(1, childAgentId);
            statement.setInt(2, parentAgentId);
        }
    }

    private static class InsertTraitAsDoubleOperation implements UpdateOperation {
        private final int agentId;
        private final short geneNameId;
        private final Double allele;

        public InsertTraitAsDoubleOperation(final int agentId, final short geneNameId, final Double allele) {
            this.agentId = agentId;
            this.geneNameId = geneNameId;
            this.allele = allele;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO quantitative_traits (agent_id, trait_name_id, value) VALUES (?, ?, ?)";
        }

        @Override
        public void update(final PreparedStatement statement) throws SQLException {
            statement.setInt(1, agentId);
            statement.setShort(2, geneNameId);
            statement.setDouble(3, allele);
        }
    }

    private static class InsertTraitAsStringOperation implements UpdateOperation {
        private final int agentId;
        private final short geneNameId;
        private final short traitValueId;

        public InsertTraitAsStringOperation(final int agentId, final short geneNameId, final short traitValueId) {
            this.agentId = agentId;
            this.geneNameId = geneNameId;
            this.traitValueId = traitValueId;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO discrete_traits (agent_id, trait_name_id, trait_value_id) VALUES (?, ?, ?)";
        }

        @Override
        public void update(final PreparedStatement statement) throws SQLException {
            statement.setInt(1, agentId);
            statement.setShort(2, geneNameId);
            statement.setShort(3, traitValueId);
        }
    }

    private static class InsertNameOperation implements UpdateOperation {
        private final String name;
        private final short id;

        public InsertNameOperation(final short id, final String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO names (id, name) VALUES(?, ?)";
        }

        @Override
        public void update(final PreparedStatement statement) throws SQLException {
            statement.setShort(1, id);
            statement.setString(2, name);
        }
    }

    private static class NameIdMap {
        private final Map<String, Short> map = Maps.newHashMap();
        private short maxId = 0;

        public short create(final String s) {
            assert s != null;

            if (map.containsKey(s))
                throw new IllegalArgumentException("Key already exists: " + s);

            final short value = ++maxId;
            final Short previous = map.put(s, value);
            assert previous == null;

            return value;
        }

        public boolean contains(final String name) {
            return map.containsKey(name);
        }

        public short get(final String name) {
            return map.get(name);
        }
    }
}
