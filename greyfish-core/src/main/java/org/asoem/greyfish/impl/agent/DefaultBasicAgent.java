package org.asoem.greyfish.impl.agent;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageBox;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.AbstractAgent;
import org.asoem.greyfish.core.agent.ActionExecutionStrategy;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.impl.simulation.BasicSimulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default implementation of {@code BasicAgent}.
 */
public final class DefaultBasicAgent extends AbstractAgent<BasicAgent, BasicSimulation>
        implements BasicAgent {
    private final Population population;
    private final FunctionalList<AgentAction<BasicAgent>> actions;
    private final FunctionalList<AgentTrait<BasicAgent, ?>> traits;
    private final FunctionalList<AgentProperty<BasicAgent, ?>> properties;
    private final MessageBox<ACLMessage<BasicAgent>> inBox;
    private final ActionExecutionStrategy actionExecutionStrategy;

    private Set<Integer> parents;
    private SimulationContext<BasicSimulation, BasicAgent> simulationContext;

    private DefaultBasicAgent(final DefaultBasicAgent original, final DeepCloner cloner) {
        cloner.addClone(original, this);
        this.population = original.population;
        this.actions = ImmutableFunctionalList.copyOf(Iterables.transform(original.actions, new Function<AgentAction<BasicAgent>, AgentAction<BasicAgent>>() {
            @Nullable
            @Override
            public AgentAction<BasicAgent> apply(@Nullable final AgentAction<BasicAgent> input) {
                return cloner.getClone(input);
            }
        }));
        this.traits = ImmutableFunctionalList.copyOf(Iterables.transform(original.traits, new Function<AgentTrait<BasicAgent, ?>, AgentTrait<BasicAgent, ?>>() {
            @Nullable
            @Override
            public AgentTrait<BasicAgent, ?> apply(@Nullable final AgentTrait<BasicAgent, ?> input) {
                return cloner.getClone(input);
            }
        }));
        this.properties = ImmutableFunctionalList.copyOf(Iterables.transform(original.properties, new Function<AgentProperty<BasicAgent, ?>, AgentProperty<BasicAgent, ?>>() {
            @Nullable
            @Override
            public AgentProperty<BasicAgent, ?> apply(@Nullable final AgentProperty<BasicAgent, ?> input) {
                return cloner.getClone(input);
            }
        }));
        this.parents = original.parents;
        this.simulationContext = original.simulationContext;
        this.inBox = original.inBox;
        this.actionExecutionStrategy = original.actionExecutionStrategy;
    }

    @Override
    protected BasicAgent self() {
        return this;
    }

    @Override
    public FunctionalList<AgentTrait<BasicAgent, ?>> getTraits() {
        return traits;
    }

    @Override
    public Set<Integer> getParents() {
        return parents;
    }

    @Override
    public void reproduce(final Chromosome chromosome) {
        throw new UnsupportedOperationException("deprecated");
    }

    @Override
    public void setParents(final Set<Integer> parents) {
        this.parents = checkNotNull(parents);
    }

    @Override
    public FunctionalList<AgentProperty<BasicAgent, ?>> getProperties() {
        return properties;
    }

    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public FunctionalList<AgentAction<BasicAgent>> getActions() {
        return actions;
    }

    @Override
    protected SimulationContext<BasicSimulation, BasicAgent> getSimulationContext() {
        return simulationContext;
    }

    @Override
    protected MessageBox<ACLMessage<BasicAgent>> getInBox() {
        return inBox;
    }

    @Override
    protected void setSimulationContext(final SimulationContext<BasicSimulation, BasicAgent> simulationContext) {
        this.simulationContext = checkNotNull(simulationContext);
    }

    @Override
    protected ActionExecutionStrategy getActionExecutionStrategy() {
        return actionExecutionStrategy;
    }

    @Override
    public DeepCloneable deepClone(final DeepCloner cloner) {
        return new DefaultBasicAgent(this, cloner);
    }
}
