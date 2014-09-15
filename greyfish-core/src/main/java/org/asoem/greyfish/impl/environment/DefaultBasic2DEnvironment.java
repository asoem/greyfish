package org.asoem.greyfish.impl.environment;

import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.agent.DefaultActiveContext;
import org.asoem.greyfish.core.environment.Generic2DEnvironment;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.utils.space.Point2D;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultBasic2DEnvironment
        extends Generic2DEnvironment<Basic2DAgent, Basic2DEnvironment, BasicTiled2DSpace, Point2D>
        implements Basic2DEnvironment {
    private final AtomicInteger agentIdSequence = new AtomicInteger();

    private DefaultBasic2DEnvironment(final Builder builder) {
        super(builder);
    }

    @Override
    protected void activateAgent(final Basic2DAgent agent) {
        agent.activate(DefaultActiveContext.create(self(), agentIdSequence.incrementAndGet(), getTime()));
    }

    @Override
    protected Basic2DEnvironment self() {
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
            extends Basic2DSimulationBuilder<Builder, DefaultBasic2DEnvironment, Basic2DEnvironment, Basic2DAgent, BasicTiled2DSpace, Point2D> {

        public Builder(final BasicTiled2DSpace space, final Set<Basic2DAgent> prototypes) {
            super(space, prototypes);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected DefaultBasic2DEnvironment checkedBuild() {
            return new DefaultBasic2DEnvironment(this);
        }
    }
}
