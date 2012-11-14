package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.AgentTraits;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.base.InheritableBuilder;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.space.*;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 16:20
 */
@Root(name = "agent")
abstract class AbstractAgent<S extends Simulation<S, A, Z, P>, A extends Agent<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> implements Agent<S, A, Z, P> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(AbstractAgent.class);

    @Element(name = "properties")
    private final SearchableList<AgentProperty<?>> properties;

    @Element(name = "actions")
    private final SearchableList<AgentAction<A,S,Z,P>> actions;

    @Element(name = "traits")
    private final SearchableList<AgentTrait<?>> traits;

    private final ActionExecutionStrategy actionExecutionStrategy;

    private final AgentMessageBox<A> inBox;

    @Element(name = "population")
    @Nullable
    private Population population;

    @Element(name = "projection", required = false)
    @Nullable
    private P projection;

    @Element(name = "motion", required = false)
    private Motion2D motion = ImmutableMotion2D.noMotion();

    @Element(name = "simulationContext", required = false)
    private SimulationContext<S,A,Z,P> simulationContext = PassiveSimulationContext.instance();

    private Set<Integer> parents = Collections.emptySet();

    protected AbstractAgent(AbstractAgent abstractAgent, final DeepCloner cloner, AgentInitializationFactory agentInitializationFactory) {
        cloner.addClone(abstractAgent, this);
        // share
        this.population = abstractAgent.population;
        // clone
        this.actions = agentInitializationFactory.newSearchableList(Iterables.transform(abstractAgent.actions, new Function<AgentAction, AgentAction>() {
            @Override
            public AgentAction apply(@Nullable AgentAction agentAction) {
                return cloner.getClone(agentAction, AgentAction.class);
            }
        }));
        this.properties = agentInitializationFactory.newSearchableList(Iterables.transform(abstractAgent.properties, new Function<AgentProperty<?>, AgentProperty<?>>() {
            @Override
            public AgentProperty<?> apply(@Nullable AgentProperty<?> agentProperty) {
                return cloner.getClone(agentProperty, AgentProperty.class);
            }
        }));
        this.traits = agentInitializationFactory.newSearchableList(Iterables.transform(abstractAgent.traits, new Function<AgentTrait<?>, AgentTrait<?>>() {
            @Override
            public AgentTrait<?> apply(@Nullable AgentTrait<?> agentTrait) {
                return cloner.getClone(agentTrait, AgentTrait.class);
            }
        }));
        // reconstruct
        this.actionExecutionStrategy = checkNotNull(agentInitializationFactory.createStrategy(actions));
        this.inBox = checkNotNull(agentInitializationFactory.createMessageBox());
    }

    protected AbstractAgent(AbstractBuilder<? extends AbstractAgent, ? extends AbstractBuilder> builder, AgentInitializationFactory agentInitializationFactory) {
        this.properties = agentInitializationFactory.newSearchableList(builder.properties);
        for (AgentProperty property : builder.properties) {
            property.setAgent(this);
        }
        this.actions = agentInitializationFactory.newSearchableList(builder.actions);
        for (AgentAction action : builder.actions) {
            action.setAgent(this);
        }
        this.traits = agentInitializationFactory.newSearchableList(builder.traits);
        for (AgentTrait<?> trait : builder.traits) {
            trait.setAgent(this);
        }
        this.population = builder.population;
        this.actionExecutionStrategy = checkNotNull(agentInitializationFactory.createStrategy(actions));
        this.inBox = checkNotNull(agentInitializationFactory.createMessageBox());
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
    public boolean hasPopulation(@Nullable Population population) {
        return Objects.equal(this.population, population);
    }

    private <E extends AgentComponent> boolean addComponent(SearchableList<E> list, E element) {
        if (list.add(element)) {
            element.setAgent(this);
            return true;
        }
        return false;
    }

    private static <E extends AgentComponent> boolean removeComponent(SearchableList<? extends E> list, E element) {
        if (list.remove(element)) {
            element.setAgent(null);
            return true;
        }
        return false;
    }

    private static void clearComponentList(SearchableList<? extends AgentComponent> list) {
        List<AgentComponent> temp = ImmutableList.copyOf(list);
        list.clear();
        for (AgentComponent component : temp)
            component.setAgent(null);
    }

    @Override
    public boolean addAction(AgentAction action) {
        return addComponent(actions, action);
    }

    @Override
    public boolean removeAction(AgentAction action) {
        return removeComponent(actions, action);
    }

    @Override
    public void removeAllActions() {
        clearComponentList(actions);
    }

    @Override
    public <T extends AgentAction<A,S,Z,P>> T getAction(String name, Class<T> clazz) {
        return checkNotNull(clazz).cast(findByName(actions, name));
    }

    @Override
    public boolean addProperty(AgentProperty property) {
        return addComponent(properties, property);
    }

    @Override
    public boolean removeProperty(AgentProperty property) {
        return removeComponent(properties, property);
    }

    @Override
    public void removeAllProperties() {
        clearComponentList(properties);
    }

    @Override
    public <T extends AgentProperty> T getProperty(String name, Class<T> clazz) {
        return checkNotNull(clazz).cast(findByName(properties, name));
    }

    @Override
    public AgentProperty<?> findProperty(Predicate<? super AgentProperty<?>> predicate) {
        return properties.find(predicate);
    }

    @Override
    public boolean addTrait(AgentTrait<?> gene) {
        return addComponent(traits, gene);
    }

    @Override
    public boolean removeGene(AgentTrait<?> gene) {
        return removeComponent(traits, gene);
    }

    @Override
    public void removeAllGenes() {
        clearComponentList(traits);
    }

    @Override
    public SearchableList<AgentTrait<?>> getTraits() {
        return traits;
    }

    @Override
    @Nullable
    public <T extends AgentTrait> T getTrait(String name, Class<T> clazz) {
        return checkNotNull(clazz).cast(findByName(traits, name));
    }

    @Override
    public void changeActionExecutionOrder(AgentAction object, AgentAction object2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void receive(AgentMessage message) {
        LOGGER.debug("{} received a message: {}", this, message);
        inBox.push(message);
    }

    @Override
    public void receiveAll(Iterable<? extends AgentMessage<A>> messages) {
        LOGGER.debug("{} received {} messages: {}", this, Iterables.size(messages), messages);
        inBox.pushAll(messages);
    }

    @Override
    public int getId() {
        return simulationContext.getAgentId();
    }

    @Override
    public int getTimeOfBirth() {
        return simulationContext.getActivationStep();
    }

    @Override
    public int getAge() {
        return simulationContext.getAge();
    }

    @Override
    @Nullable
    public Color getColor() {
        return (population != null) ? population.getColor() : null;
    }

    @Override
    public void setColor(Color color) {
    }

    @Override
    public Iterable<AgentMessage<A>> getMessages(MessageTemplate template) {
        return inBox.consume(template);
    }

    @Override
    public boolean hasMessages(MessageTemplate template) {
        return Iterables.any(inBox, template);
    }

    @Override
    public void logEvent(Object eventOrigin, String title, String message) {
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        simulationContext.logEvent(this, eventOrigin, title, message);
    }

    @Override
    public void execute() {
        actionExecutionStrategy.execute();
        LOGGER.info("{} executed {}", this, actionExecutionStrategy.lastExecutedAction());
    }

    @Override
    public void shutDown(SimulationContext<S, A, Z, P> context) {
        checkNotNull(context);
        this.simulationContext = context;
        inBox.clear();
    }

    @Override
    public boolean isActive() {
        return simulationContext.isActiveContext();
    }

    @Override
    public S simulation() {
        checkState(isActive(), "A passive Agent has no associated simulation");
        return simulationContext.getSimulation();
    }

    @Override
    public void freeze() {
        throw new UnsupportedOperationException();
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
    public void activate(SimulationContext<S, A, Z, P> context) {
        checkNotNull(context);
        simulationContext = context;
        actionExecutionStrategy.reset();
        logEvent(this, "activated", "");
    }

    @Override
    public void initialize() {
        for (AgentNode node : children()) {
            node.initialize();
        }
    }

    @Override
    public SearchableList<AgentProperty<?>> getProperties() {
        return properties;
    }

    @Override
    public SearchableList<AgentAction<A,S,Z,P>> getActions() {
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
    public boolean didCollide() {
        if (projection == null)
            throw new IllegalStateException("This agent has no projection");
        return projection.didCollide();
    }

    @Override
    public Set<Integer> getParents() {
        return parents;
    }

    @Override
    public int getSimulationStep() {
        return simulationContext.getSimulationStep();
    }

    @Override
    public void reproduce(Initializer<? super A> initializer) {
        simulation().createAgent(population, initializer);
    }

    @Override
    public Iterable<A> getAllAgents() {
        return simulation().getAgents();
    }

    @Override
    public void die() {
        simulation().removeAgent(self());
    }

    @Override
    public AgentTrait<?> findTrait(Predicate<? super AgentTrait<?>> traitPredicate) {
        return traits.find(traitPredicate);
    }

    @Override
    public void setMotion(Motion2D motion) {
        this.motion = checkNotNull(motion);
    }

    @Override
    public void updateGeneComponents(Chromosome chromosome) {
        checkNotNull(chromosome);
        AgentTraits.updateValues(traits, ImmutableList.copyOf(Iterables.transform(chromosome.getGenes(), new Function<Gene<?>, Object>() {
            @Override
            public Object apply(@Nullable Gene<?> o) {
                assert o != null;
                return o.getAllele();
            }
        })));
        parents = checkNotNull(chromosome.getParents());
    }

    @Override
    public String toString() {
        return "Agent[" + population + ']' + "#" + simulationContext.getAgentId() + "@" + simulationContext.getSimulationStep();
    }

    @Override
    public Iterable<AgentNode> children() {
        return Iterables.<AgentNode>concat(
                getProperties(),
                getActions(),
                getTraits()
        );
    }

    @Override
    public AgentNode parent() {
        return null;
    }

    private static <E extends HasName> E findByName(SearchableList<E> searchableList, final String name) {
        return searchableList.find(new Predicate<HasName>() {
            @Override
            public boolean apply(HasName agentAction) {
                return agentAction.getName().equals(name);
            }
        });
    }

    protected static abstract class AbstractBuilder<A extends Agent<S,A,Z,P>, S extends Simulation<S,A,Z,P>, Z extends Space2D<A,P>, P extends Object2D, E extends AbstractAgent, T extends AbstractBuilder<A,S,Z,P,E,T>> extends InheritableBuilder<E, T> implements Serializable {
        private final Population population;
        private final List<AgentAction<A,S,Z,P>> actions = Lists.newArrayList();
        private final List<AgentProperty<?>> properties = Lists.newArrayList();
        private final List<AgentTrait<?>> traits = Lists.newArrayList();

        protected AbstractBuilder(Population population) {
            this.population = checkNotNull(population, "Population must not be null");
        }

        protected AbstractBuilder(AbstractAgent abstractAgent) {
            this.population = abstractAgent.population;
            this.actions.addAll(abstractAgent.actions);
            this.properties.addAll(abstractAgent.properties);
            this.traits.addAll(abstractAgent.traits);
        }

        public T addTraits(AgentTrait<?>... traits) {
            this.traits.addAll(asList(checkNotNull(traits)));
            return self();
        }

        public T addTraits(Iterable<? extends AgentTrait<?>> traits) {
            Iterables.addAll(this.traits, checkNotNull(traits));
            return self();
        }

        public T addActions(AgentAction<A,S,Z,P>... actions) {
            this.actions.addAll(asList(checkNotNull(actions)));
            return self();
        }

        public T addActions(Iterable<? extends AgentAction<A,S,Z,P>> actions) {
            Iterables.addAll(this.actions, checkNotNull(actions));
            return self();
        }

        public T addProperties(AgentProperty<?>... properties) {
            this.properties.addAll(asList(checkNotNull(properties)));
            return self();
        }

        public T addProperties(Iterable<? extends AgentProperty<?>> properties) {
            Iterables.addAll(this.properties, checkNotNull(properties));
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            final Iterable<String> nameWithPossibleDuplicates = Iterables.transform(Iterables.concat(actions, properties, traits), new Function<AgentComponent, String>() {
                @Override
                public String apply(AgentComponent input) {
                    return input.getName();
                }
            });
            final String duplicate = Iterables.find(nameWithPossibleDuplicates, new Predicate<String>() {
                private final Set<String> nameSet = Sets.newHashSet();

                @Override
                public boolean apply(@Nullable String input) {
                    return ! nameSet.add(input);
                }
            }, null);
            checkState(duplicate == null, "You assigned the following name more than once to a component: " + duplicate);
        }
    }
}
