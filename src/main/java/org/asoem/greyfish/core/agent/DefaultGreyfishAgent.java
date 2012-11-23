package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulation;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.space.Point2D;

import java.io.Serializable;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:37
 */
public class DefaultGreyfishAgent extends ForwardingAgent<DefaultGreyfishSimulation, DefaultGreyfishAgent, DefaultGreyfishSpace, Point2D> implements Serializable {
    private final Agent<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D> delegate;

    public DefaultGreyfishAgent(Population population) {
        this.delegate = FrozenAgent.<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D, DefaultGreyfishSpace>builder(population).self(this).build();
    }

    public DefaultGreyfishAgent(DefaultGreyfishAgent defaultGreyfishAgent, DeepCloner cloner) {
        cloner.addClone(defaultGreyfishAgent, this);
        delegate = (Agent<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D>) cloner.getClone(defaultGreyfishAgent.delegate);
    }

    @Override
    protected Agent<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D> delegate() {
        return delegate;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DefaultGreyfishAgent(this, cloner);
    }
}
