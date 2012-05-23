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

    @Inject
    private H2Logger(@Assisted Simulation simulation) {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:~/"+simulation.getUUID().toString(), "sa", "");

            connection.createStatement().execute(
                    "CREATE TABLE agents (id int NOT NULL PRIMARY KEY, population VARCHAR(255) NOT NULL, activated_at int NOT NULL, created_at TIMESTAMP NOT NULL)");
            connection.createStatement().execute(
                    "CREATE TABLE chromosome_tree (id int NOT NULL, parent_id int NOT NULL)"
            );
            connection.createStatement().execute(
                    "CREATE TABLE genes_double (agent_id int NOT NULL, name VARCHAR(80) NOT NULL, value DOUBLE NOT NULL)");
            connection.createStatement().execute(
                    "CREATE TABLE genes_string (agent_id int NOT NULL, name VARCHAR(80) NOT NULL, value VARCHAR(255) NOT NULL)");
            connection.createStatement().execute(
                    "CREATE TABLE agent_events (id int NOT NULL PRIMARY KEY, created_at TIMESTAMP NOT NULL, simulation_step int NOT NULL, agent_id int NOT NULL, source VARCHAR(255) NOT NULL, title VARCHAR(255) NOT NULL, message VARCHAR(255) NOT NULL)");

            connection.setAutoCommit(false);

        } catch (SQLException e) {
            close();
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
                }, -1, GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW, -1);
    }

    @Override
    public void close() {
        try {
            commit();
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
            final String insertAgentQuery = "INSERT INTO agents (id, population, activated_at, created_at) VALUES (?, ?, ?, ?)";
            final PreparedStatement insertAgentStatement = borrowStatement(insertAgentQuery);
            insertAgentStatement.setInt(1, agent.getId());
            insertAgentStatement.setString(2, agent.getPopulation().getName());
            insertAgentStatement.setInt(3, agent.getSimulationContext().getActivationStep());
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

            final String insertGeneAsDoubleQuery = "INSERT INTO genes_double (agent_id, name, value) VALUES (?, ?, ?)";
            final PreparedStatement insertGeneAsDoubleStatement = borrowStatement(insertGeneAsDoubleQuery);
            final String insertGeneAsStringQuery = "INSERT INTO genes_string (agent_id, name, value) VALUES (?, ?, ?)";
            final PreparedStatement insertGeneAsStringStatement = borrowStatement(insertGeneAsStringQuery);

            for (GeneComponent<?> gene : agent.getGeneComponentList()) {
                assert gene != null;

                if (Double.class.equals(gene.getSupplierClass())) {
                    insertGeneAsDoubleStatement.setInt(1, agent.getId());
                    insertGeneAsDoubleStatement.setString(2, gene.getName());
                    insertGeneAsDoubleStatement.setDouble(3, ((GeneComponent<Double>) gene).getValue());
                    insertGeneAsDoubleStatement.execute();
                }
                else {
                    insertGeneAsStringStatement.setInt(1, agent.getId());
                    insertGeneAsStringStatement.setString(2, gene.getName());
                    insertGeneAsStringStatement.setString(3, String.valueOf(gene.getValue()));
                    insertGeneAsStringStatement.execute();

                }
            }

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
            final String insertEventQuery = "INSERT INTO agent_events (id, created_at, simulation_step, agent_id, source, title, message) VALUES (?, ?, ?, ?, ?, ?, ?)";
            final PreparedStatement insertEventStatement = borrowStatement(insertEventQuery);
            insertEventStatement.setInt(1, eventId);
            insertEventStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

            //insertEventStatement.setBytes(1, Bytes.concat(Longs.toByteArray(uuid.getMostSignificantBits()), Longs.toByteArray(uuid.getLeastSignificantBits())));
            insertEventStatement.setInt(3, currentStep);
            insertEventStatement.setInt(4, agentId);
            // coordinates
            insertEventStatement.setString(5, source);
            insertEventStatement.setString(6, title);
            insertEventStatement.setString(7, message);

            insertEventStatement.execute();

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
