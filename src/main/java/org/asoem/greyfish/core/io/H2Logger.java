package org.asoem.greyfish.core.io;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
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
    private int event_commit_counter = 0;
    private final PreparedStatement insertAgentStatement;
    private final PreparedStatement insertGeneAsStringStatement;
    private final PreparedStatement insertEventStatement;
    private final PreparedStatement insertGeneAsDoubleStatement;
    private final PreparedStatement insertChromosomeStatement;

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

            insertChromosomeStatement = connection.prepareStatement(
                    "INSERT INTO chromosome_tree (id, parent_id) VALUES (?, ?)"
            );

            insertAgentStatement = connection.prepareStatement(
                    "INSERT INTO agents (id, population, activated_at, created_at) VALUES (?, ?, ?, ?)"
            );
            insertGeneAsStringStatement = connection.prepareStatement(
                    "INSERT INTO genes_string (agent_id, name, value) VALUES (?, ?, ?)"
            );
            insertGeneAsDoubleStatement = connection.prepareStatement(
                    "INSERT INTO genes_double (agent_id, name, value) VALUES (?, ?, ?)"
            );
            insertEventStatement = connection.prepareStatement(
                    "INSERT INTO agent_events (id, created_at, simulation_step, agent_id, source, title, message) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );

        } catch (SQLException e) {
            close();
            throw new IOError(e);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("The H2 db library seems not to be loaded");
        }
    }

    @Override
    public void close() {
        try {
            commit();
            connection.close();
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    @Override
    public void addAgent(Agent agent) {
        try {

            insertAgentStatement.setInt(1, agent.getId());
            insertAgentStatement.setString(2, agent.getPopulation().getName());
            insertAgentStatement.setInt(3, agent.getSimulationContext().getActivationStep());
            insertAgentStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            insertAgentStatement.execute();

            insertChromosomeStatement.setInt(1, agent.getId());
            for (Integer parentId : agent.getGeneComponentList().getOrigin().getParents()) {
                insertChromosomeStatement.setInt(2, parentId);
                insertChromosomeStatement.execute();
            }

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

        } catch (SQLException e) {
            throw new IOError(e);
        }

        if (shouldCommit()) {
            commit();
        }
    }

    @Override
    public void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
        try {
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
