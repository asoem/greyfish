package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.utils.space.Location2D;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 12:54
 */
public class AgentEvent {

    private int agent;
    private Object source;
    private String key;
    private String value;
    private int step;
    private String simulation;
    private String populationName;
    private Location2D locatable2D;

    public AgentEvent(String simulationId, int step, int agentId, String populationName, Object source, String key, String value, Location2D locatable2D) {
        this.simulation = simulationId;
        this.populationName = populationName;
        this.locatable2D = checkNotNull(locatable2D);
        this.value = checkNotNull(value);
        this.key = checkNotNull(key);
        this.source = checkNotNull(source);
        this.agent = checkNotNull(agentId);
        this.step = step;
    }

    private AgentEvent() {
    }

    public int getAgentId() {
        return agent;
    }

    public Object getSource() {
        return source;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getStep() {
        return step;
    }

    public String getSimulationId() {
        return simulation;
    }

    public Location2D getLocatable2D() {
        return locatable2D;
    }

    public String getPopulationName() {
        return populationName;
    }
}
