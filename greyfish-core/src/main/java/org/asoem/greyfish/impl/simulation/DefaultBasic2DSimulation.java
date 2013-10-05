package org.asoem.greyfish.impl.simulation;

import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.simulation.Generic2DSimulation;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.space.DefaultGreyfishTiled2DSpace;
import org.asoem.greyfish.utils.space.Point2D;

import java.util.Set;

public final class DefaultBasic2DSimulation
        extends Generic2DSimulation<Basic2DAgent, Basic2DSimulation, DefaultGreyfishTiled2DSpace, Point2D>
        implements Basic2DSimulation {

    private DefaultBasic2DSimulation(final Builder builder) {
        super(builder);
    }

    @Override
    protected Basic2DSimulation self() {
        return this;
    }

    public static Builder builder(final DefaultGreyfishTiled2DSpace space, final Basic2DAgent prototype) {
        return new Builder(space, ImmutableSet.of(prototype));
    }

    public static Builder builder(final DefaultGreyfishTiled2DSpace space, final Set<Basic2DAgent> prototypes) {
        return new Builder(space, prototypes);
    }

    @Override
    public void createAgent(final Population population, final Point2D location) {
        enqueueAgentCreation(population, location);
    }

    @Override
    public void createAgent(final Population population, final Point2D location, final Chromosome chromosome) {
        enqueueAgentCreation(population, chromosome, location);
    }

    public static final class Builder
            extends Basic2DSimulationBuilder<Builder, DefaultBasic2DSimulation, Basic2DSimulation, Basic2DAgent, DefaultGreyfishTiled2DSpace, Point2D> {

        public Builder(final DefaultGreyfishTiled2DSpace space, final Set<Basic2DAgent> prototypes) {
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
