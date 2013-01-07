package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulation;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.utils.base.CloneMap;
import org.asoem.greyfish.utils.space.Point2D;

import java.io.Serializable;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:37
 */
public class DefaultGreyfishAgentImpl extends ForwardingSpatialAgent<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D> implements DefaultGreyfishAgent, Serializable {

    private final SpatialAgent<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D> delegate;

    @SuppressWarnings("unchecked") // casting a clone is safe
    private DefaultGreyfishAgentImpl(DefaultGreyfishAgentImpl defaultGreyfishAgent, CloneMap cloner) {
        cloner.addClone(defaultGreyfishAgent, this);
        delegate = (SpatialAgent<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D>) cloner.getClone(defaultGreyfishAgent.delegate);
    }

    private DefaultGreyfishAgentImpl(Builder builder) {
        this.delegate = builder.builderDelegate
                .self(this)
                .build();
    }

    @Override
    protected SpatialAgent<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D> delegate() {
        return delegate;
    }

    @Override
    public DefaultGreyfishAgentImpl deepClone(CloneMap cloneMap) {
        return new DefaultGreyfishAgentImpl(this, cloneMap);
    }

    public static Builder builder(Population population) {
        return new Builder(population);
    }

    public static class Builder {
        private final FrozenAgent.Builder<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D, DefaultGreyfishSpace> builderDelegate;

        public Builder(Population population) {
            builderDelegate = FrozenAgent.builder(population);
        }

        public Builder addAction(AgentAction<DefaultGreyfishAgent> action) {
            builderDelegate.addAction(action);
            return this;
        }

        public Builder addActions(AgentAction<DefaultGreyfishAgent> ... actions) {
            builderDelegate.addActions(actions);
            return this;
        }

        public Builder addProperties(AgentProperty<DefaultGreyfishAgent, ?> ... properties) {
            builderDelegate.addProperties(properties);
            return this;
        }

        public Builder addTraits(AgentTrait<DefaultGreyfishAgent, ?> ... traits) {
            builderDelegate.addTraits(traits);
            return this;
        }

        public DefaultGreyfishAgentImpl build() {
            return new DefaultGreyfishAgentImpl(this);
        }
    }
}
