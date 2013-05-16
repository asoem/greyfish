package org.asoem.greyfish.core.io;

import com.google.common.collect.Maps;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.DoublePrecisionRealNumberTrait;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Point2D;

import javax.annotation.Nullable;
import java.io.IOError;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 23.04.12
 * Time: 14:49
 */
public class H2Logger<A extends SpatialAgent<A, ?, ?>> implements SimulationLogger<A> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(H2Logger.class);
    private static final int COMMIT_THRESHOLD = 1000;
    private final Connection connection;
    private final List<UpdateOperation> updateOperationList = new ArrayList<UpdateOperation>(COMMIT_THRESHOLD);
    private final NameIdMap nameIdMap = new NameIdMap();

    public H2Logger(String path) {
        checkNotNull(path, "path is null");
        Connection connection = null;

        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(
                    String.format("jdbc:h2:%s;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;DB_CLOSE_ON_EXIT=FALSE",
                            path), "sa", "");
            LOGGER.info("Connection opened to database {}", path);
            this.connection = connection;
            initDatabase();
            connection.setAutoCommit(false);
        }
        catch (SQLException e) {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e1) {
                LOGGER.warn("Exception during connection.close()", e1);
            }
            throw new IOError(e);
        }
        catch (ClassNotFoundException e) {
            throw new AssertionError("The H2 database driver could not be found");
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    finalizeAndShutdownDatabase();
                } catch (SQLException e) {
                    LOGGER.warn("Exception while finalizing the database", e);
                }
            }
        });
    }

    private void initDatabase() throws SQLException {
        Statement statement = connection.createStatement();
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
        } finally {
            closeStatement(statement);
        }
    }

    private void finalizeAndShutdownDatabase() throws SQLException {
        LOGGER.info("Finalizing and shutting down the database");

        commit();
        connection.setAutoCommit(true);

        Statement statement = connection.createStatement();
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
            for (UpdateOperation updateOperation : updateOperationList) {
                final String sql = updateOperation.sqlString();
                if (!preparedStatementMap.containsKey(sql)) {
                    preparedStatementMap.put(sql, connection.prepareStatement(sql));
                }
                final PreparedStatement preparedStatement = preparedStatementMap.get(sql);
                updateOperation.update(preparedStatement);
                preparedStatement.addBatch();
            }
            updateOperationList.clear();

            for (PreparedStatement preparedStatement : preparedStatementMap.values()) {
                preparedStatement.executeBatch();
            }

            connection.commit();
        } finally {
            for (PreparedStatement preparedStatement : preparedStatementMap.values()) {
                closeStatement(preparedStatement);
            }
        }
    }

    private boolean shouldCommit() {
        return updateOperationList.size() >= COMMIT_THRESHOLD;
    }

    private static void closeStatement(@Nullable Statement statement) {
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

    private void addUpdateOperation(UpdateOperation updateOperation) {
        updateOperationList.add(updateOperation);
    }

    private short idForName(String name) {
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
    public void logAgentCreation(A agent) {
        addUpdateOperation(new InsertAgentOperation(agent.getId(), idForName(agent.getPopulation().getName()), agent.getTimeOfBirth()));
        final Set<Integer> parents = agent.getParents();
        for (Integer parentId : parents) {
            addUpdateOperation(new InsertChromosomeOperation(agent.getId(), parentId));
        }
        for (AgentTrait<?, ?> trait : agent.getTraits()) {
            assert trait != null;
            if (trait instanceof DoublePrecisionRealNumberTrait) {
                addUpdateOperation(new InsertGeneAsDoubleOperation(agent.getId(), idForName(trait.getName()), (Double) trait.get()));
            } else {
                addUpdateOperation(new InsertGeneAsStringOperation(agent.getId(), idForName(trait.getName()), idForName(String.valueOf(trait.get()))));
            }
        }
        tryCommit();
    }

    @Override
    public void logAgentEvent(A agent, int currentStep, String source, String title, String message) {
        addUpdateOperation(new InsertEventOperation(currentStep, agent.getId(), agent.getProjection(), idForName(source), idForName(title), message));
        tryCommit();
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

        public InsertEventOperation(int currentStep, int agentId, Object2D coordinate, short sourceNameId, short titleNameId, String message) {
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
        public void update(PreparedStatement statement) throws SQLException {
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

        public InsertAgentOperation(int id, short populationNameId, int timeOfBirth) {
            this.id = id;
            this.populationNameId = populationNameId;
            this.timeOfBirth = timeOfBirth;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO agents (id, population_name_id, activated_at) VALUES (?, ?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setShort(2, populationNameId);
            statement.setInt(3, timeOfBirth);
        }
    }

    private static class InsertChromosomeOperation implements UpdateOperation {
        private final int childAgentId;
        private final int parentAgentId;

        public InsertChromosomeOperation(int childAgentId, int parentAgentId) {
            this.childAgentId = childAgentId;
            this.parentAgentId = parentAgentId;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO chromosome_tree (child_id, parent_id) VALUES (?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, childAgentId);
            statement.setInt(2, parentAgentId);
        }
    }

    private static class InsertGeneAsDoubleOperation implements UpdateOperation {
        private final int agentId;
        private final short geneNameId;
        private final Double allele;

        public InsertGeneAsDoubleOperation(int agentId, short geneNameId, Double allele) {
            this.agentId = agentId;
            this.geneNameId = geneNameId;
            this.allele = allele;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO quantitative_traits (agent_id, trait_name_id, value) VALUES (?, ?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, agentId);
            statement.setShort(2, geneNameId);
            statement.setDouble(3, allele);
        }
    }

    private static class InsertGeneAsStringOperation implements UpdateOperation {
        private final int agentId;
        private final short geneNameId;
        private final short traitValueId;

        public InsertGeneAsStringOperation(int agentId, short geneNameId, short traitValueId) {
            this.agentId = agentId;
            this.geneNameId = geneNameId;
            this.traitValueId = traitValueId;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO discrete_traits (agent_id, trait_name_id, trait_value_id) VALUES (?, ?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, agentId);
            statement.setShort(2, geneNameId);
            statement.setShort(3, traitValueId);
        }
    }

    private static class InsertNameOperation implements UpdateOperation {
        private final String name;
        private final short id;

        public InsertNameOperation(short id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO names (id, name) VALUES(?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setShort(1, id);
            statement.setString(2, name);
        }
    }

    private static class NameIdMap {
        private final Map<String, Short> map = Maps.newHashMap();
        private short maxId = 0;

        public short create(String s) {
            assert s != null;

            if (map.containsKey(s))
                throw new IllegalArgumentException("Key already exists: " + s);

            final short value = ++maxId;
            final Short previous = map.put(s, value);
            assert previous == null;

            return value;
        }

        public boolean contains(String name) {
            return map.containsKey(name);
        }

        public short get(String name) {
            return map.get(name);
        }
    }
}
