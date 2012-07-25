package org.asoem.greyfish.core.io;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import java.io.IOError;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: christoph
 * Date: 23.04.12
 * Time: 14:49
 */
public class H2Logger implements SimulationLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2Logger.class);
    private final Connection connection;
    private final List<UpdateOperation> updateOperationList = Lists.newArrayList();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final NameIdMap nameIdMap = new NameIdMap();

    @Inject
    private H2Logger(@Assisted Simulation simulation) {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(String.format("jdbc:h2:~/greyfish-data/%s;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;TRACE_LEVEL_FILE=2", simulation.getUUID().toString()), "sa", "");

            connection.createStatement().execute(
                    "CREATE TABLE agents (" +
                            "id INT NOT NULL PRIMARY KEY," +
                            "population_name_id INT NOT NULL," +
                            "activated_at INT NOT NULL," +
                            "created_at TIMESTAMP NOT NULL)");
            connection.createStatement().execute(
                    "CREATE TABLE chromosome_tree (" +
                            "id INT NOT NULL," +
                            "parent_id INT NOT NULL)");
            connection.createStatement().execute(
                    "CREATE TABLE names (" +
                            "id int NOT NULL PRIMARY KEY," +
                            "name VARCHAR(255) UNIQUE NOT NULL);");
            connection.createStatement().execute(
                    "CREATE TABLE genes_double (" +
                            "agent_id INT NOT NULL," +
                            "gene_name_id INT NOT NULL," +
                            "value DOUBLE NOT NULL)");
            connection.createStatement().execute(
                    "CREATE TABLE genes_string (" +
                            "agent_id INT NOT NULL, " +
                            "gene_name_id INT NOT NULL, " +
                            "value VARCHAR(255) NOT NULL)");
            connection.createStatement().execute(
                    "CREATE TABLE agent_events (" +
                            "id INT NOT NULL PRIMARY KEY, " +
                            "simulation_step int NOT NULL, " +
                            "agent_id INT NOT NULL, " +
                            "source_name_id INT NOT NULL, " +
                            "title_name_id INT NOT NULL, " +
                            "message VARCHAR(255) NOT NULL, " +
                            "x DOUBLE NOT NULL, " +
                            "y DOUBLE NOT NULL " +
                            ")");

            connection.setAutoCommit(false);

        } catch (SQLException e) {
            throw new IOError(e);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("The H2 database driver could not be found");
        }
    }

    @Override
    public void close() {
        try {
            commit();

            connection.createStatement().execute(
                    "CREATE INDEX ON genes_double(agent_id)");
            connection.createStatement().execute(
                    "CREATE INDEX ON genes_double(gene_name_id)");
            connection.createStatement().execute(
                    "CREATE INDEX ON genes_string(agent_id)");
            connection.createStatement().execute(
                    "CREATE INDEX ON genes_string(gene_name_id)");
            connection.createStatement().execute(
                    "CREATE INDEX ON agent_events(agent_id)");
            connection.createStatement().execute(
                    "CREATE INDEX ON agent_events(title_name_id)");
            connection.createStatement().execute(
                    "CREATE INDEX ON agent_events(simulation_step)");
            connection.createStatement().execute(
                    "CREATE UNIQUE HASH INDEX ON names(name)");

            commit();

        } catch (SQLException e) {
            throw new IOError(e);
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.warn("Exception during Connection.close()", e);
            }
        }
    }

    @Override
    public void addAgent(Agent agent) {
        lock.readLock().lock();
        try{
            addUpdateOperation(new InsertAgentOperation(agent.getId(), idForName(agent.getPopulation().getName()), agent.getTimeOfBirth(), new Timestamp(System.currentTimeMillis())));
            final Set<Integer> parents = agent.getGeneComponentList().getOrigin().getParents();
            for (Integer parentId : parents) {
                addUpdateOperation(new InsertChromosomeOperation(agent.getId(), parentId));
            }
            for (GeneComponent<?> gene : agent.getGeneComponentList()) {
                assert gene != null;
                if (Double.class.equals(gene.getAlleleClass())) {
                    addUpdateOperation(new InsertGeneAsDoubleOperation(agent.getId(), idForName(gene.getName()), ((GeneComponent<Double>) gene).getAllele()));
                } else {
                    addUpdateOperation(new InsertGeneAsStringOperation(agent.getId(), idForName(gene.getName()), String.valueOf(gene.getAllele())));
                }
            }
            tryCommit();
        } finally {
            lock.readLock().unlock();
        }
    }

    private int idForName(String name) {
        if (nameIdMap.contains(name)) {
            return nameIdMap.get(name);
        }
        else {
            final int id = nameIdMap.create(name);
            addUpdateOperation(new InsertNameOperation(id, name));
            return id;
        }
    }

    @Override
    public void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
        lock.readLock().lock();
        try {
            addUpdateOperation(new InsertEventOperation(eventId, uuid, currentStep, agentId, idForName(populationName), coordinates, idForName(source), idForName(title), message));
            tryCommit();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void addUpdateOperation(UpdateOperation updateOperation) {
        updateOperationList.add(updateOperation);
    }

    private void tryCommit() {
        if (shouldCommit()) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (shouldCommit()) {
                    commit();
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    private void commit() {
        lock.writeLock().lock();

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

            for (PreparedStatement preparedStatement : preparedStatementMap.values()) {
                preparedStatement.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            throw new IOError(e);
        } finally {
            for (PreparedStatement preparedStatement : preparedStatementMap.values()) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            updateOperationList.clear();
            lock.writeLock().unlock();
        }
    }

    private boolean shouldCommit() {
        return updateOperationList.size() >= 1000;
    }

    private static interface UpdateOperation {
        public String sqlString();
        public void update(PreparedStatement statement) throws SQLException;
    }

    private static class InsertEventOperation implements UpdateOperation {
        private final int eventId;
        private final UUID uuid;
        private final int currentStep;
        private final int agentId;
        private final int populationNameId;
        private final double[] coordinates;
        private final int sourceNameId;
        private final int titleNameId;
        private final String message;

        public InsertEventOperation(int eventId, UUID uuid, int currentStep, int agentId, int populationNameId, double[] coordinates, int sourceNameId, int titleNameId, String message) {

            this.eventId = eventId;
            this.uuid = uuid;
            this.currentStep = currentStep;
            this.agentId = agentId;
            this.populationNameId = populationNameId;
            this.coordinates = coordinates;
            assert coordinates.length >= 2;
            this.sourceNameId = sourceNameId;
            this.titleNameId = titleNameId;
            this.message = message;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO agent_events (id, simulation_step, agent_id, source_name_id, title_name_id, message, x, y) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            assert statement.getParameterMetaData().getParameterCount() == 8;
            statement.setInt(1, eventId);
            statement.setInt(2, currentStep);
            statement.setInt(3, agentId);
            statement.setInt(4, sourceNameId);
            statement.setInt(5, titleNameId);
            statement.setString(6, message);
            statement.setDouble(7, coordinates[0]);
            statement.setDouble(8, coordinates[1]);
        }
    }

    private static class InsertAgentOperation implements UpdateOperation {
        private final int id;
        private final int nameId;
        private final int timeOfBirth;
        private final Timestamp timestamp;

        public InsertAgentOperation(int id, int nameId, int timeOfBirth, Timestamp timestamp) {
            this.id = id;
            this.nameId = nameId;
            this.timeOfBirth = timeOfBirth;
            this.timestamp = timestamp;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO agents (id, population_name_id, activated_at, created_at) VALUES (?, ?, ?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setInt(2, nameId);
            statement.setInt(3, timeOfBirth);
            statement.setTimestamp(4, timestamp);
        }
    }

    private static class InsertChromosomeOperation implements UpdateOperation {
        private final int id;
        private final int origin;

        public InsertChromosomeOperation(int id, int origin) {
            this.id = id;
            this.origin = origin;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO chromosome_tree (id, parent_id) VALUES (?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setInt(2, origin);
        }
    }

    private static class InsertGeneAsDoubleOperation implements UpdateOperation {
        private final int id;
        private final int nameId;
        private final Double allele;

        public InsertGeneAsDoubleOperation(int id, int nameId, Double allele) {
            this.id = id;
            this.nameId = nameId;
            this.allele = allele;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO genes_double (agent_id, gene_name_id, value) VALUES (?, ?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setInt(2, nameId);
            statement.setDouble(3, allele);
        }
    }

    private static class InsertGeneAsStringOperation implements UpdateOperation {
        private final int id;
        private final int nameId;
        private final String s;

        public InsertGeneAsStringOperation(int id, int nameId, String s) {
            this.id = id;
            this.nameId = nameId;
            this.s = s;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO genes_string (agent_id, gene_name_id, value) VALUES (?, ?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setInt(2, nameId);
            statement.setString(3, s);
        }
    }

    private static class InsertNameOperation implements UpdateOperation {
        private final String name;
        private final int id;

        public InsertNameOperation(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO names (id, name) VALUES(?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setString(2, name);
        }
    }

    private static class NameIdMap {
        private final Map<String, Integer> map = Maps.newConcurrentMap();
        private final AtomicInteger maxId = new AtomicInteger(0);

        public int create(String s) {
            assert s != null;
            final int id = maxId.incrementAndGet();
            map.put(s, id);
            return id;
        }

        public boolean contains(String name) {
            return map.containsKey(name);
        }

        public int get(String name) {
            return map.get(name);
        }
    }
}
