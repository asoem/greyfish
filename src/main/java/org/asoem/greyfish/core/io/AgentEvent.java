package org.asoem.greyfish.core.io;

import ch.systemsx.cisd.hdf5.CompoundElement;
import com.google.common.primitives.Doubles;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.Date;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 12:54
 */

@Entity
public class AgentEvent {

    @PrimaryKey
    private int eventId;

    private long simulationUUID_upper;

    private long simulationUUID_lower;

    private int agentId;

    private Date createdAt;

    private int simulationStep;

    @CompoundElement(dimensions = 20)
    private String sourceOfEvent;

    @CompoundElement(dimensions = 20)
    private String agentPopulationName;

    @CompoundElement(dimensions = 2)
    private double[] locationInSpace;

    @CompoundElement(dimensions = 20)
    private String eventTitle;

    @CompoundElement(dimensions = 20)
    private String eventMessage;

    public AgentEvent(int eventId, UUID simulationId, int simulationStep, int agentId, String agentPopulationName, double[] locatable2D, String sourceOfEvent, String eventTitle, String eventMessage) {
        this.eventId = eventId;
        this.agentPopulationName = agentPopulationName;
        this.locationInSpace = checkNotNull(locatable2D);
        this.eventMessage = checkNotNull(eventMessage);
        this.eventTitle = checkNotNull(eventTitle);
        this.sourceOfEvent = checkNotNull(sourceOfEvent);
        this.simulationStep = simulationStep;
        this.simulationUUID_lower = simulationId.getLeastSignificantBits();
        this.simulationUUID_upper = simulationId.getMostSignificantBits();
        this.agentId = checkNotNull(agentId);
        this.createdAt = new Date();
    }

    @SuppressWarnings("UnusedDeclaration") // used for deserialization
    private AgentEvent() {
    }

    public int getAgentId() {
        return agentId;
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

    public UUID getSimulationId() {
        return new UUID(simulationUUID_upper, simulationUUID_lower);
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

    public long getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return "AgentEvent{" +
                "eventId=" + eventId +
                ", simulationUUID =" + getSimulationId() +
                ", agentId =" + agentId +
                ", createdAt=" + createdAt +
                ", simulationStep=" + simulationStep +
                ", sourceOfEvent='" + sourceOfEvent + '\'' +
                ", agentPopulationName='" + agentPopulationName + '\'' +
                ", locationInSpace=[" + Doubles.join(" ", locationInSpace) + "]" +
                ", eventTitle='" + eventTitle + '\'' +
                ", eventMessage='" + eventMessage + '\'' +
                '}';
    }
}
