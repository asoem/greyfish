package org.asoem.greyfish.core.io;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;

import java.io.IOError;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: christoph
 * Date: 23.04.12
 * Time: 14:49
 */
public class H2Logger implements SimulationLogger {

    private final Connection connection;
    private final List<UpdateOperation> updateOperationList = Lists.newArrayList();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Inject
    private H2Logger(@Assisted Simulation simulation) {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(String.format("jdbc:h2:~/greyfish-data/%s;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0", simulation.getUUID().toString()), "sa", "");

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
                            "id int NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(255) UNIQUE NOT NULL);" +
                            "CREATE UNIQUE HASH INDEX ON names(name)");
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
            throw new AssertionError("The H2 db library seems not to be loaded");
        }
    }

    @Override
    public void close() {
        try {
            commit();

            connection.createStatement().execute(
                    "CREATE INDEX ON genes_double(agent_id); CREATE INDEX ON genes_double(gene_name_id);");

            connection.createStatement().execute(
                    "CREATE INDEX ON genes_string(agent_id); CREATE INDEX ON genes_string(gene_name_id);");

            connection.createStatement().execute(
                    "CREATE INDEX ON agent_events(agent_id); CREATE INDEX ON agent_events(title_name_id);");

            commit();

        } catch (SQLException e) {
            throw new IOError(e);
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void addAgent(Agent agent) {
        lock.readLock().lock();
        try{
            addUpdateOperation(new InsertNameOperation(agent.getPopulation().getName()));
            addUpdateOperation(new InsertAgentOperation(agent.getId(), agent.getPopulation().getName(), agent.getTimeOfBirth(), new Timestamp(System.currentTimeMillis())));
            for (Integer parentId : agent.getGeneComponentList().getOrigin().getParents()) {
                addUpdateOperation(new InsertChromosomeOperation(agent.getId(), parentId));
            }
            for (GeneComponent<?> gene : agent.getGeneComponentList()) {
                assert gene != null;
                if (Double.class.equals(gene.getSupplierClass())) {
                    addUpdateOperation(new InsertNameOperation(gene.getName()));
                    addUpdateOperation(new InsertGeneAsDoubleOperation(agent.getId(), gene.getName(), ((GeneComponent<Double>) gene).getAllele()));
                } else {
                    addUpdateOperation(new InsertNameOperation(gene.getName()));
                    addUpdateOperation(new InsertGeneAsStringOperation(agent.getId(), gene.getName(), String.valueOf(gene.getAllele())));
                }
            }
            tryCommit();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
        lock.readLock().lock();
        try {
            addUpdateOperation(new InsertNameOperation(populationName));
            addUpdateOperation(new InsertNameOperation(source));
            addUpdateOperation(new InsertNameOperation(title));
            addUpdateOperation(new InsertEventOperation(eventId, uuid, currentStep, agentId, populationName, coordinates, source, title, message));
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
        private final String populationName;
        private final double[] coordinates;
        private final String source;
        private final String title;
        private final String message;

        public InsertEventOperation(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {

            this.eventId = eventId;
            this.uuid = uuid;
            this.currentStep = currentStep;
            this.agentId = agentId;
            this.populationName = populationName;
            this.coordinates = coordinates;
            assert coordinates.length >= 2;
            this.source = source;
            this.title = title;
            this.message = message;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO agent_events (id, simulation_step, agent_id, source_name_id, title_name_id, message, x, y) VALUES (?, ?, ?, (SELECT id FROM names WHERE name = ?), (SELECT id FROM names WHERE name = ?), ?, ?, ?);";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            assert statement.getParameterMetaData().getParameterCount() == 8;
            statement.setInt(1, eventId);
            statement.setInt(2, currentStep);
            statement.setInt(3, agentId);
            statement.setString(4, source);
            statement.setString(5, title);
            statement.setString(6, message);
            statement.setDouble(7, coordinates[0]);
            statement.setDouble(8, coordinates[1]);
        }
    }

    private static class InsertAgentOperation implements UpdateOperation {
        private final int id;
        private final String name;
        private final int timeOfBirth;
        private final Timestamp timestamp;

        public InsertAgentOperation(int id, String name, int timeOfBirth, Timestamp timestamp) {
            this.id = id;
            this.name = name;
            this.timeOfBirth = timeOfBirth;
            this.timestamp = timestamp;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO agents (id, population_name_id, activated_at, created_at) VALUES (?, (SELECT id FROM names WHERE name = ?), ?, ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setString(2, name);
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
        private final String name;
        private final Double allele;

        public InsertGeneAsDoubleOperation(int id, String name, Double allele) {
            this.id = id;
            this.name = name;
            this.allele = allele;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO genes_double (agent_id, gene_name_id, value) VALUES (?, (SELECT id FROM names WHERE name = ?), ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setDouble(3, allele);
        }
    }

    private static class InsertGeneAsStringOperation implements UpdateOperation {
        private final int id;
        private final String name;
        private final String s;

        public InsertGeneAsStringOperation(int id, String name, String s) {
            this.id = id;
            this.name = name;
            this.s = s;
        }

        @Override
        public String sqlString() {
            return "INSERT INTO genes_string (agent_id, gene_name_id, value) VALUES (?, (SELECT id FROM names WHERE name = ?), ?)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, s);
        }
    }

    private class InsertNameOperation implements UpdateOperation {
        private final String name;

        public InsertNameOperation(String name) {
            this.name = name;
        }

        @Override
        public String sqlString() {
            return "SET @name = ?; INSERT INTO names (name) SELECT @name WHERE NOT EXISTS (SELECT * FROM names WHERE name = @name)";
        }

        @Override
        public void update(PreparedStatement statement) throws SQLException {
            statement.setString(1, name);
        }
    }
}
