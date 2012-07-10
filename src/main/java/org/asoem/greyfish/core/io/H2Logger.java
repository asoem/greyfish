package org.asoem.greyfish.core.io;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;

import java.io.IOError;
import java.sql.*;
import java.util.UUID;

/**
 * User: christoph
 * Date: 23.04.12
 * Time: 14:49
 */
public class H2Logger implements SimulationLogger {

    private final Connection connection;
    private final KeyedObjectPool<String, PreparedStatement> statementPool;

    private int event_commit_counter = 0;
    private String insertNameIfNotExistsQuery = "SET @name = ?; INSERT INTO names (name) SELECT @name WHERE NOT EXISTS (SELECT * FROM names WHERE name = @name)";

    @Inject
    private H2Logger(@Assisted Simulation simulation) {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:~/greyfish-data/" + simulation.getUUID().toString(), "sa", "");

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
                            "name VARCHAR(255) UNIQUE NOT NULL)");
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

        statementPool = new GenericKeyedObjectPool<String, PreparedStatement>(
                new BaseKeyedPoolableObjectFactory<String, PreparedStatement>() {
                    @Override
                    public PreparedStatement makeObject(String sql) throws Exception {
                        return connection.prepareStatement(sql);
                    }

                    @Override
                    public void passivateObject(String key, PreparedStatement obj) throws Exception {
                        obj.clearParameters();
                    }
                }, -1, GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW, -1);
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

            statementPool.clear();
            connection.close();
        } catch (SQLException e) {
            throw new IOError(e);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void addAgent(Agent agent) {
        try {
            final PreparedStatement insertNameIfNotExistsStatement = borrowStatement(insertNameIfNotExistsQuery);
            insertNameIfNotExistsStatement.setString(1, agent.getPopulation().getName());
            insertNameIfNotExistsStatement.execute();

            final String insertAgentQuery = "INSERT INTO agents (id, population_name_id, activated_at, created_at) VALUES (?, (SELECT id FROM names WHERE name = ?), ?, ?);";
            final PreparedStatement insertAgentStatement = borrowStatement(insertAgentQuery);
            insertAgentStatement.setInt(1, agent.getId());
            insertAgentStatement.setString(2, agent.getPopulation().getName());
            insertAgentStatement.setInt(3, agent.getTimeOfBirth());
            insertAgentStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            insertAgentStatement.execute();
            returnStatement(insertAgentQuery, insertAgentStatement);

            final String insertChromosomeQuery = "INSERT INTO chromosome_tree (id, parent_id) VALUES (?, ?)";
            final PreparedStatement insertChromosomeStatement = borrowStatement(insertChromosomeQuery);
            insertChromosomeStatement.setInt(1, agent.getId());
            for (Integer parentId : agent.getGeneComponentList().getOrigin().getParents()) {
                insertChromosomeStatement.setInt(2, parentId);
                insertChromosomeStatement.execute();
            }
            returnStatement(insertChromosomeQuery, insertChromosomeStatement);


            final String insertGeneAsDoubleQuery = "INSERT INTO genes_double (agent_id, gene_name_id, value) VALUES (?, (SELECT id FROM names WHERE name = ?), ?)";
            final PreparedStatement insertGeneAsDoubleStatement = borrowStatement(insertGeneAsDoubleQuery);
            final String insertGeneAsStringQuery = "INSERT INTO genes_string (agent_id, gene_name_id, value) VALUES (?, (SELECT id FROM names WHERE name = ?), ?)";
            final PreparedStatement insertGeneAsStringStatement = borrowStatement(insertGeneAsStringQuery);

            for (GeneComponent<?> gene : agent.getGeneComponentList()) {
                assert gene != null;

                insertNameIfNotExistsStatement.setString(1, gene.getName());
                insertNameIfNotExistsStatement.execute();

                if (Double.class.equals(gene.getSupplierClass())) {
                    insertGeneAsDoubleStatement.setInt(1, agent.getId());
                    insertGeneAsDoubleStatement.setString(2, gene.getName());
                    insertGeneAsDoubleStatement.setDouble(3, ((GeneComponent<Double>) gene).getAllele());
                    insertGeneAsDoubleStatement.execute();
                } else {
                    insertGeneAsStringStatement.setInt(1, agent.getId());
                    insertGeneAsStringStatement.setString(2, gene.getName());
                    insertGeneAsStringStatement.setString(3, String.valueOf(gene.getAllele()));
                    insertGeneAsStringStatement.execute();

                }
            }

            returnStatement(insertNameIfNotExistsQuery, insertNameIfNotExistsStatement);
            returnStatement(insertGeneAsDoubleQuery, insertGeneAsDoubleStatement);
            returnStatement(insertGeneAsStringQuery, insertGeneAsStringStatement);

        } catch (SQLException e) {
            throw new IOError(e);
        }

        if (shouldCommit()) {
            commit();
        }
    }

    private void returnStatement(String query, PreparedStatement statement) {
        try {
            statementPool.returnObject(query, statement);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private PreparedStatement borrowStatement(String s) {
        try {
            return statementPool.borrowObject(s);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
        try {
            final PreparedStatement insertNameIfNotExistsStatement = borrowStatement(insertNameIfNotExistsQuery);
            insertNameIfNotExistsStatement.setString(1, source);
            insertNameIfNotExistsStatement.execute();
            insertNameIfNotExistsStatement.setString(1, title);
            insertNameIfNotExistsStatement.execute();

            final String insertEventQuery = "INSERT INTO agent_events (id, simulation_step, agent_id, source_name_id, title_name_id, message, x, y) VALUES (?, ?, ?, (SELECT id FROM names WHERE name = ?), (SELECT id FROM names WHERE name = ?), ?, ?, ?)";
            final PreparedStatement insertEventStatement = borrowStatement(insertEventQuery);
            insertEventStatement.setInt(1, eventId);

            //insertEventStatement.setBytes(1, Bytes.concat(Longs.toByteArray(uuid.getMostSignificantBits()), Longs.toByteArray(uuid.getLeastSignificantBits())));
            insertEventStatement.setInt(2, currentStep);
            insertEventStatement.setInt(3, agentId);
            // coordinates
            insertEventStatement.setString(4, source);
            insertEventStatement.setString(5, title);
            insertEventStatement.setString(6, message);
            assert coordinates.length >= 2;
            insertEventStatement.setDouble(7, coordinates[0]);
            insertEventStatement.setDouble(8, coordinates[1]);

            insertEventStatement.execute();

            returnStatement(insertNameIfNotExistsQuery, insertNameIfNotExistsStatement);
            returnStatement(insertEventQuery, insertEventStatement);
        } catch (SQLException e) {
            throw new IOError(e);
        }

        if (shouldCommit()) {
            commit();
        }
    }

    private boolean shouldCommit() {
        final boolean shouldCommit = ++event_commit_counter == 1000;
        if (shouldCommit)
            event_commit_counter = 0;
        return shouldCommit;
    }

    private void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
