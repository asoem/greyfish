package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.Locatable2D;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 12:54
 */
public class AgentEvent {
    private final Agent agent;
    private final Object source;
    private final String key;
    private final String value;
    private final int step;
    private final Simulation simulation;
    private final Locatable2D locatable2D;

    public AgentEvent(Simulation simulation, int step, Agent agent, Object source, String key, String value, Locatable2D locatable2D) {
        this.simulation = simulation;
        this.locatable2D = checkNotNull(locatable2D);
        this.value = checkNotNull(value);
        this.key = checkNotNull(key);
        this.source = checkNotNull(source);
        this.agent = checkNotNull(agent);
        this.step = step;
    }

    public Agent getAgent() {
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

    public Simulation getSimulation() {
        return simulation;
    }

    public Locatable2D getLocatable2D() {
        return locatable2D;
    }
}
