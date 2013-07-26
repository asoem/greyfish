package org.asoem.greyfish.core.simulation;

import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.Point2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:46
 */
public class DefaultGreyfishSimulationImpl extends Basic2DSimulation<DefaultGreyfishAgent, DefaultGreyfishSimulation, DefaultGreyfishSpace, Point2D> implements DefaultGreyfishSimulation {

    private DefaultGreyfishSimulationImpl(final Builder builder) {
        super(builder);
    }

    @Override
    protected DefaultGreyfishSimulation self() {
        return this;
    }

    public static Builder builder(final DefaultGreyfishSpace space, final DefaultGreyfishAgent prototype) {
        return new Builder(space, ImmutableSet.of(prototype));
    }

    public static Builder builder(final DefaultGreyfishSpace space, final Set<DefaultGreyfishAgent> prototypes) {
        return new Builder(space, prototypes);
    }

    @Override
    public void createAgent(final Population population) {
        createAgent(population, ImmutablePoint2D.at(RandomGenerators.nextDouble(RandomGenerators.rng(), 0.0, getSpace().width()), RandomGenerators.nextDouble(RandomGenerators.rng(), 0.0, getSpace().height())));
    }

    @Override
    public void createAgent(final Population population, final Point2D location) {
        enqueueAgentCreation(population, location);
    }

    @Override
    public void createAgent(final Population population, final Point2D location, final Chromosome chromosome) {
        enqueueAgentCreation(population, chromosome, location);
    }

    public static class Builder extends Basic2DSimulationBuilder<Builder, DefaultGreyfishSimulationImpl, DefaultGreyfishSimulation, DefaultGreyfishAgent, DefaultGreyfishSpace, Point2D> {

        public Builder(final DefaultGreyfishSpace space, final Set<DefaultGreyfishAgent> prototypes) {
            super(space, prototypes);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected DefaultGreyfishSimulationImpl checkedBuild() {
            return new DefaultGreyfishSimulationImpl(this);
        }
    }
}
