package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulation;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.space.Point2D;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:37
 */
public class DefaultGreyfishAgent extends ForwardingAgent<DefaultGreyfishSimulation, DefaultGreyfishAgent, DefaultGreyfishSpace, Point2D> {

    private final Agent<DefaultGreyfishSimulation, DefaultGreyfishAgent, Point2D> agent;

    public DefaultGreyfishAgent(Agent<DefaultGreyfishSimulation, DefaultGreyfishAgent, Point2D> agent) {
        this.agent = agent;
    }

    private DefaultGreyfishAgent(DefaultGreyfishAgent defaultGreyfishAgent, DeepCloner cloner) {
        this.agent = (Agent<DefaultGreyfishSimulation, DefaultGreyfishAgent, Point2D>) cloner.getClone(defaultGreyfishAgent.agent);
    }

    @Override
    protected Agent<DefaultGreyfishSimulation, DefaultGreyfishAgent, Point2D> delegate() {
        return agent;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DefaultGreyfishAgent(this, cloner);
    }
}
