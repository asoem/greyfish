package org.asoem.greyfish.impl.simulation;

import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.agent.DefaultActiveSimulationContext;
import org.asoem.greyfish.core.simulation.Generic2DSimulation;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.utils.space.Point2D;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultBasic2DSimulation
        extends Generic2DSimulation<Basic2DAgent, Basic2DSimulation, BasicTiled2DSpace, Point2D>
        implements Basic2DSimulation {
    private final AtomicInteger agentIdSequence = new AtomicInteger();

    private DefaultBasic2DSimulation(final Builder builder) {
        super(builder);
    }

    @Override
    protected void activateAgent(final Basic2DAgent agent) {
        agent.activate(DefaultActiveSimulationContext.create(self(), agentIdSequence.incrementAndGet(), getTime()));
    }

    @Override
    protected Basic2DSimulation self() {
        return this;
    }

    public static Builder builder(final BasicTiled2DSpace space, final Basic2DAgent prototype) {
        return new Builder(space, ImmutableSet.of(prototype));
    }

    public static Builder builder(final BasicTiled2DSpace space, final Set<Basic2DAgent> prototypes) {
        return new Builder(space, prototypes);
    }

    @Override
    public void enqueueAddition(final Basic2DAgent agent, final Point2D point2D) {
        enqueueAgentCreation(agent, point2D);
    }

    public static final class Builder
            extends Basic2DSimulationBuilder<Builder, DefaultBasic2DSimulation, Basic2DSimulation, Basic2DAgent, BasicTiled2DSpace, Point2D> {

        public Builder(final BasicTiled2DSpace space, final Set<Basic2DAgent> prototypes) {
            super(space, prototypes);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected DefaultBasic2DSimulation checkedBuild() {
            return new DefaultBasic2DSimulation(this);
        }
    }
}
