package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.Point2D;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:46
 */
public class DefaultGreyfishSimulationImpl extends Basic2DSimulation<DefaultGreyfishAgent, DefaultGreyfishSimulation, DefaultGreyfishSpace, Point2D> implements DefaultGreyfishSimulation {

    private DefaultGreyfishSimulationImpl(Builder builder) {
        super(builder);
    }

    @Override
    protected DefaultGreyfishSimulation self() {
        return this;
    }

    public static Builder builder(DefaultGreyfishSpace space, Set<DefaultGreyfishAgent> prototypes) {
        return new Builder(space, prototypes);
    }

    @Override
    public void createAgent(Population population) {
        createAgent(population, ImmutablePoint2D.at(RandomUtils.nextDouble(0.0, getSpace().width()), RandomUtils.nextDouble(0.0, getSpace().height())));
    }

    @Override
    public void createAgent(Population population, Point2D location) {
        createAgent(population, location, null);
    }

    @Override
    public void createAgent(Population population, Point2D location, @Nullable Chromosome chromosome) {
        enqueueAgentCreation(population, chromosome, location);
    }

    public static class Builder extends ParallelizedSimulationBuilder<Builder, DefaultGreyfishSimulationImpl, DefaultGreyfishSimulation, DefaultGreyfishAgent, DefaultGreyfishSpace, Point2D> {

        public Builder(DefaultGreyfishSpace space, final Set<DefaultGreyfishAgent> prototypes) {
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
