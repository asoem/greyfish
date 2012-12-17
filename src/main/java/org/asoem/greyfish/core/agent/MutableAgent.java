package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedLists;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class MutableAgent<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> extends AbstractSpatialAgent<A, S, P> {

    private final SearchableList<AgentProperty<A, ?>> properties;
    private final SearchableList<AgentAction<A>> actions;
    private final SearchableList<AgentTrait<A, ?>> traits;
    private final ActionExecutionStrategy actionExecutionStrategy;
    private final AgentMessageBox<A> inBox;
    @Nullable
    private Population population;
    @Nullable
    private P projection;
    private Motion2D motion = ImmutableMotion2D.noMotion();
    private SimulationContext<S,A> simulationContext = PassiveSimulationContext.<S, A>instance();
    private Set<Integer> parents = Collections.emptySet();
    private final A self;

    @SuppressWarnings("unchecked") // casting a clone is safe
    public MutableAgent(MutableAgent<A, S, Z, P> frozenAgent, final DeepCloner cloner) {
        cloner.addClone(frozenAgent, this);
        // share
        this.population = frozenAgent.population;
        // clone
        this.actions = AugmentedLists.newAugmentedArrayList(Iterables.transform(frozenAgent.actions, new Function<AgentAction<A>, AgentAction<A>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentAction<A> apply(@Nullable AgentAction<A> agentAction) {
                return (AgentAction<A>) cloner.getClone(agentAction);
            }
        }));
        this.properties = AugmentedLists.newAugmentedArrayList(Iterables.transform(frozenAgent.properties, new Function<AgentProperty<A, ?>, AgentProperty<A, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentProperty<A, ?> apply(@Nullable AgentProperty<A, ?> agentProperty) {
                return (AgentProperty<A, ?>) cloner.getClone(agentProperty);
            }
        }));
        this.traits = AugmentedLists.newAugmentedArrayList(Iterables.transform(frozenAgent.traits, new Function<AgentTrait<A, ?>, AgentTrait<A, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentTrait<A, ?> apply(@Nullable AgentTrait<A, ?> agentTrait) {
                return (AgentTrait<A, ?>) cloner.getClone(agentTrait);
            }
        }));
        // reconstruct
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FixedSizeMessageBox<A>();
        this.self = (A) cloner.getClone(frozenAgent.self);
    }

    public MutableAgent(Population population) {
        this.properties = AugmentedLists.newAugmentedArrayList();
        this.actions = AugmentedLists.newAugmentedArrayList();
        this.traits = AugmentedLists.newAugmentedArrayList();
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FixedSizeMessageBox<A>();
        this.self = null; // TODO: where to get self from?
        setPopulation(population);
    }

    @Override
    protected A self() {
        return self;
    }

    @Nullable
    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(@Nullable Population population) {
        this.population = population;
    }

    @Override
    public SearchableList<AgentTrait<A, ?>> getTraits() {
        return traits;
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public Motion2D getMotion() {
        return motion;
    }

    @Override
    public SearchableList<AgentProperty<A, ?>> getProperties() {
        return properties;
    }

    @Override
    public SearchableList<AgentAction<A>> getActions() {
        return actions;
    }

    @Nullable
    @Override
    public P getProjection() {
        return projection;
    }

    @Override
    public void setProjection(@Nullable P projection) {
        this.projection = projection;
    }

    @Override
    public Set<Integer> getParents() {
        return parents;
    }

    @Override
    public void setMotion(Motion2D motion) {
        this.motion = checkNotNull(motion);
    }

    @Override
    public String toString() {
        return "Agent[" + getPopulation() + ']' + "#" + getSimulationContext().getAgentId() + "@" + getSimulationContext().getSimulationStep();
    }

    @Override
    protected SimulationContext<S, A> getSimulationContext() {
        return simulationContext;
    }

    @Override
    protected AgentMessageBox<A> getInBox() {
        return inBox;
    }

    @Override
    protected void setSimulationContext(SimulationContext<S, A> simulationContext) {
        this.simulationContext = simulationContext;
    }

    @Override
    protected ActionExecutionStrategy getActionExecutionStrategy() {
        return actionExecutionStrategy;
    }

    @Override
    protected void setParents(Set<Integer> parents) {
        this.parents = parents;
    }

    @Override
    public MutableAgent<A, S, Z, P> deepClone(DeepCloner cloner) {
        return new MutableAgent<A, S, Z, P>(this, cloner);
    }

    @Override
    public void changeActionExecutionOrder(final AgentAction<A> object, final AgentAction<A> object2) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(object2);
        if ( ! getActions().contains(object) || ! getActions().contains(object2))
            throw new IllegalArgumentException();
        int index1 = getActions().indexOf(object);
        int index2 = getActions().indexOf(object2);
        getActions().add(index2, getActions().remove(index1));
    }
}
