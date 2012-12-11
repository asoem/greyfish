package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.asoem.greyfish.core.agent.ActiveSimulationContext;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.PassiveSimulationContext;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:46
 */
public class DefaultGreyfishSimulationImpl extends ForwardingSpatialSimulation<DefaultGreyfishAgent, DefaultGreyfishSpace> implements DefaultGreyfishSimulation {

    private final SpatialSimulation<DefaultGreyfishAgent, DefaultGreyfishSpace> delegate;

    public DefaultGreyfishSimulationImpl(DefaultGreyfishSpace space, final Set<DefaultGreyfishAgent> prototypes) {
        this.delegate = ParallelizedSimulation.builder(space, prototypes)
                .agentActivator(new AgentActivator<DefaultGreyfishAgent>() {
                    private final AtomicInteger agentIdSequence = new AtomicInteger();

                    @Override
                    public void activate(DefaultGreyfishAgent agent) {
                        agent.activate(ActiveSimulationContext.<DefaultGreyfishSimulation, DefaultGreyfishAgent>create(DefaultGreyfishSimulationImpl.this, agentIdSequence.incrementAndGet(), getStep() + 1));
                    }

                    @Override
                    public void deactivate(DefaultGreyfishAgent agent) {
                        agent.deactivate(PassiveSimulationContext.<DefaultGreyfishSimulation, DefaultGreyfishAgent>instance());
                    }
                })
                .agentPool(new GenericKeyedObjectPool<Population, DefaultGreyfishAgent>(new BaseKeyedPoolableObjectFactory<Population, DefaultGreyfishAgent>() {

                    Map<Population, DefaultGreyfishAgent> map = Maps .uniqueIndex(prototypes, new Function<DefaultGreyfishAgent, Population>() {
                        @Nullable
                        @Override
                        public Population apply(DefaultGreyfishAgent input) {
                            return input.getPopulation();
                        }
                    });

                    @Override
                    public DefaultGreyfishAgent makeObject(Population population) throws Exception {
                        return DeepCloner.clone(map.get(population), DefaultGreyfishAgent.class);
                    }
                }))
                .build();
    }

    @Override
    protected SpatialSimulation<DefaultGreyfishAgent, DefaultGreyfishSpace> delegate() {
        return delegate;
    }
}
