package org.asoem.greyfish.core.io;

import com.google.common.base.Strings;
import com.google.common.primitives.Doubles;
import com.sleepycat.persist.model.*;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 12:54
 */

@Entity
public class AgentEvent {

    @PrimaryKey(sequence = "agent_event_id")
    private int eventId;
    
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private EventKey eventKey;

    private Date createdAt;

    private int simulationStep;

    private String sourceOfEvent;

    private String agentPopulationName;

    private double[] locationInSpace;

    private String eventTitle;

    private String eventMessage;

    public AgentEvent(String simulationId, int simulationStep, int agentId, String agentPopulationName, String sourceOfEvent, String eventTitle, String eventMessage, double[] locatable2D) {
        this.agentPopulationName = agentPopulationName;
        this.locationInSpace = checkNotNull(locatable2D);
        this.eventMessage = checkNotNull(eventMessage);
        this.eventTitle = checkNotNull(eventTitle);
        this.sourceOfEvent = checkNotNull(sourceOfEvent);
        this.simulationStep = simulationStep;
        this.eventKey = new EventKey(checkNotNull(simulationId), checkNotNull(agentId));
        this.createdAt = new Date();
    }

    @SuppressWarnings("UnusedDeclaration") // used for deserialization
    private AgentEvent() {
    }

    public int getAgentId() {
        return eventKey.agentId;
    }

    public Object getSourceOfEvent() {
        return sourceOfEvent;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventMessage() {
        return eventMessage;
    }

    public int getSimulationStep() {
        return simulationStep;
    }

    public String getSimulationId() {
        return eventKey.simulationId;
    }

    public double[] getLocatable2D() {
        return locationInSpace;
    }

    public String getAgentPopulationName() {
        return agentPopulationName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public int getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return "AgentEvent{" +
                "eventId=" + eventId +
                ", eventKey=" + eventKey +
                ", createdAt=" + createdAt +
                ", simulationStep=" + simulationStep +
                ", sourceOfEvent='" + sourceOfEvent + '\'' +
                ", agentPopulationName='" + agentPopulationName + '\'' +
                ", locationInSpace=[" + Doubles.join(" ", locationInSpace) + "]" +
                ", eventTitle='" + eventTitle + '\'' +
                ", eventMessage='" + eventMessage + '\'' +
                '}';
    }

    @Persistent
    private static class EventKey {


        @KeyField(1)
        private String simulationId;

        @KeyField(2)
        private int agentId;

        private EventKey(String simulationId, int agentId) {
            this.simulationId = simulationId;
            this.agentId = agentId;
        }

        @SuppressWarnings("UnusedDeclaration") // used for deserialization
        private EventKey() {
        }

        @Override
        public String toString() {
            return "EventKey{" +
                    "simulationId='" + simulationId + '\'' +
                    ", agentId=" + agentId +
                    '}';
        }
    }
}
