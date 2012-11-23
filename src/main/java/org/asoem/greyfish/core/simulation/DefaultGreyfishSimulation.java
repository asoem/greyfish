package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.ActiveSimulationContext;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.PassiveSimulationContext;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:46
 */
public class DefaultGreyfishSimulation extends ForwardingSpatialSimulation<DefaultGreyfishAgent, DefaultGreyfishSpace> {

    private final SpatialSimulation<DefaultGreyfishAgent, DefaultGreyfishSpace> delegate;

    public DefaultGreyfishSimulation(DefaultGreyfishSpace space, Set<DefaultGreyfishAgent> prototypes) {
        this.delegate = ParallelizedSimulation.builder(space, prototypes)
                .agentActivator(new AgentActivator<DefaultGreyfishAgent>() {
                    private final AtomicInteger agentIdSequence = new AtomicInteger();

                    @Override
                    public void activate(DefaultGreyfishAgent agent) {
                        agent.activate(ActiveSimulationContext.create(DefaultGreyfishSimulation.this, agentIdSequence.incrementAndGet(), getStep() + 1));
                    }

                    @Override
                    public void deactivate(DefaultGreyfishAgent agent) {
                        agent.deactivate(PassiveSimulationContext.<DefaultGreyfishSimulation, DefaultGreyfishAgent>instance());
                    }
                })
                .build();
    }

    @Override
    protected SpatialSimulation<DefaultGreyfishAgent, DefaultGreyfishSpace> delegate() {
        return delegate;
    }
}
