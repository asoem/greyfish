package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.utils.space.Point2D;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:46
 */
public class DefaultGreyfishSimulation extends ForwardingSimulation<DefaultGreyfishSimulation, DefaultGreyfishAgent, DefaultGreyfishSpace, Point2D> {
    private Simulation<DefaultGreyfishSimulation, DefaultGreyfishAgent, DefaultGreyfishSpace, Point2D> simulation;

    public DefaultGreyfishSimulation(Simulation<DefaultGreyfishSimulation, DefaultGreyfishAgent, DefaultGreyfishSpace, Point2D> simulation) {
        this.simulation = simulation;
    }

    @Override
    protected Simulation<DefaultGreyfishSimulation, DefaultGreyfishAgent, DefaultGreyfishSpace, Point2D> delegate() {
        return simulation;
    }
}
