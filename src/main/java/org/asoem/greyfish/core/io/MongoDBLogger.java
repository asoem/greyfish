package org.asoem.greyfish.core.io;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.Mongo;
import org.asoem.greyfish.core.simulation.Simulation;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import java.io.IOError;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * User: christoph
 * Date: 11.04.12
 * Time: 16:11
 */
public class MongoDBLogger implements SimulationLogger {

    private final MongoCollection agent_events;
    private Mongo mongo;

    @Inject
    public MongoDBLogger(@Assisted Simulation simulation) {
        try {
            mongo = new Mongo("127.0.0.1" , 27017);
        } catch (UnknownHostException e) {
            throw new IOError(e);
        }
        Jongo jongo = new Jongo(mongo.getDB("greyfish"));
        agent_events = jongo.getCollection("agent_events");
    }

    @Override
    public void close() {
         mongo.close();
    }

    @Override
    public void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String populationName,
                         double[] coordinates, String source, String title, String message) {
        try {
            agent_events.save(new AgentEventJSON(eventId, uuid, currentStep, agentId, populationName, coordinates, source, title, message));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private static class AgentEventJSON {
        private final int eventId;
        private final UUID uuid;
        private final int currentStep;
        private final int agentId;
        private final String populationName;
        private final double[] coordinates;
        private final String source;
        private final String title;
        private final String message;

        public AgentEventJSON(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {

            this.eventId = eventId;
            this.uuid = uuid;
            this.currentStep = currentStep;
            this.agentId = agentId;
            this.populationName = populationName;
            this.coordinates = coordinates;
            this.source = source;
            this.title = title;
            this.message = message;
        }
    }
}
