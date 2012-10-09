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
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.InheritableBuilder;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.MotionObject2D;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import javax.annotation.Nullable;
import java.awt.*;
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
public abstract class AbstractAgent implements Agent {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(AbstractAgent.class);

    @Element(name = "properties")
    private final ComponentList<AgentProperty<?>> properties;

    @Element(name = "actions")
    private final ComponentList<AgentAction> actions;

    @Element(name = "traits")
    private final ComponentList<AgentTrait<?>> agentTraitList;

    private final ActionExecutionStrategy actionExecutionStrategy;

    private final AgentInitializationFactory agentInitializationFactory;

    private final AgentMessageBox inBox;

    @Element(name = "population")
    @Nullable
    private Population population;

    @Element(name = "projection", required = false)
    @Nullable
    private MotionObject2D projection;

    @Element(name = "motion", required = false)
    private Motion2D motion = ImmutableMotion2D.noMotion();

    @Element(name = "simulationContext", required = false)
    private SimulationContext simulationContext = PassiveSimulationContext.instance();

    private Set<Integer> parents = Collections.emptySet();

    protected AbstractAgent(ComponentList<AgentProperty<?>> properties,
                            ComponentList<AgentAction> actions,
                            ComponentList<AgentTrait<?>> agentTraitList,
                            AgentInitializationFactory factory) {
        this.properties = checkNotNull(properties);
        this.actions = checkNotNull(actions);
        this.agentTraitList = checkNotNull(agentTraitList);
        this.agentInitializationFactory = checkNotNull(factory);

        this.actionExecutionStrategy = checkNotNull(agentInitializationFactory.createStrategy(actions));
        this.inBox = checkNotNull(agentInitializationFactory.createMessageBox());

        initComponents();
    }

    private void initComponents() {
        for (AgentComponent component : children())
            component.setAgent(this);
    }

    @Commit
    private void commit() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    protected AbstractAgent(AbstractAgent abstractAgent, DeepCloner cloner) {
        cloner.addClone(abstractAgent, this);
        // clone
        this.actions = cloner.getClone(abstractAgent.actions, ComponentList.class);
        this.properties = cloner.getClone(abstractAgent.properties, ComponentList.class);
        this.agentTraitList = cloner.getClone(abstractAgent.agentTraitList, ComponentList.class);
        // share
        this.population = abstractAgent.population;
        this.agentInitializationFactory = abstractAgent.agentInitializationFactory;
        // reconstruct
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

    private <E extends AgentComponent> boolean addComponent(ComponentList<E> list, E element) {
        if (list.add(element)) {
            element.setAgent(this);
            return true;
        }
        return false;
    }

    private static <E extends AgentComponent> boolean removeComponent(ComponentList<? extends E> list, E element) {
        if (list.remove(element)) {
            element.setAgent(null);
            return true;
        }
        return false;
    }

    private static void clearComponentList(ComponentList<? extends AgentComponent> list) {
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
    public <T extends AgentAction> T getAction(String name, Class<T> clazz) {
        return actions.find(name, clazz);
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
        return properties.find(name, clazz);
    }

    @Override
    public AgentProperty<?> findProperty(Predicate<? super AgentProperty<?>> predicate) {
        return properties.find(predicate);
    }

    @Override
    public boolean addGene(AgentTrait<?> gene) {
        return addComponent(agentTraitList, gene);
    }

    @Override
    public boolean removeGene(AgentTrait<?> gene) {
        return removeComponent(agentTraitList, gene);
    }

    @Override
    public void removeAllGenes() {
        clearComponentList(agentTraitList);
    }

    @Override
    public ComponentList<AgentTrait<?>> getTraits() {
        return agentTraitList;
    }

    @Override
    @Nullable
    public <T extends AgentTrait> T getGene(String name, Class<T> clazz) {
        checkNotNull(clazz);

        return agentTraitList.find(name, clazz);
    }

    /**
     * WARNING: This implementation just checks if {@code object} is an {@link Agent} and shares the same population with this agent.
     * It does not check if they are or have been derived from the same prototype via {@link #deepClone}.
     */
    @Override
    public boolean isCloneOf(Object object) {
        // TODO: should be implemented differently. See JavaDoc.
        return object != null
                && Agent.class.isInstance(object) && hasPopulation(Agent.class.cast(object).getPopulation());

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
    public void receiveAll(Iterable<? extends AgentMessage> messages) {
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
    public Iterable<AgentMessage> getMessages(MessageTemplate template) {
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
    public void shutDown(PassiveSimulationContext context) {
        checkNotNull(context);
        this.simulationContext = context;
        inBox.clear();
    }

    @Override
    public boolean isActive() {
        return simulationContext.isActiveContext();
    }

    @Override
    public Simulation simulation() {
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
    public void activate(ActiveSimulationContext context) {
        checkNotNull(context);
        simulationContext = context;
        logEvent(this, "activated", "");
    }

    @Override
    public void initialize() {
        for (AgentNode node : children()) {
            node.initialize();
        }
    }

    @Override
    public ComponentList<AgentProperty<?>> getProperties() {
        return properties;
    }

    @Override
    public ComponentList<AgentAction> getActions() {
        return actions;
    }

    @Nullable
    @Override
    public MotionObject2D getProjection() {
        return projection;
    }

    @Override
    public void setProjection(@Nullable MotionObject2D projection) {
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
    public AgentTrait<?> findTrait(Predicate<? super AgentTrait<?>> traitPredicate) {
        return agentTraitList.find(traitPredicate);
    }

    @Override
    public void setMotion(Motion2D motion) {
        this.motion = checkNotNull(motion);
    }

    @Override
    public void updateGeneComponents(Chromosome chromosome) {
        checkNotNull(chromosome);
        AgentTraits.updateValues(agentTraitList, ImmutableList.copyOf(Iterables.transform(chromosome.getGenes(), new Function<Gene<?>, Object>() {
            @Override
            public Object apply(@Nullable Gene<?> o) {
                assert o != null;
                return o.getAllele();
            }
        })));
        parents = checkNotNull(chromosome.getParents());
    }

    @Override
    @Deprecated // use #children() instead
    public Iterable<AgentComponent> getComponents() {
        return children();
    }

    @Override
    public AgentComponent getComponent(final String name) {
        return Iterables.find(children(), new Predicate<AgentComponent>() {
            @Override
            public boolean apply(AgentComponent agentComponent) {
                return agentComponent.getName().equals(name);
            }
        });
    }

    @Override
    public String toString() {
        return "Agent[" + population + ']' + "#" + simulationContext.getAgentId() + "@" + simulationContext.getSimulationStep();
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Iterables.concat(
                getProperties(),
                getActions(),
                getTraits()
        );
    }

    protected static abstract class AbstractBuilder<E extends AbstractAgent, T extends AbstractBuilder<E, T>> extends InheritableBuilder<E, T> {
        protected final Population population;
        protected final List<AgentAction> actions = Lists.newArrayList();
        protected final List<AgentProperty<?>> properties = Lists.newArrayList();
        protected final List<AgentTrait<?>> traits = Lists.newArrayList();
        protected AgentInitializationFactory agentInitializationFactory = createDefaultInitializationFactory();

        protected AbstractBuilder(Population population) {
            this.population = checkNotNull(population, "Population must not be null");
        }

        // todo: these builder methods are not able to control the mutability of the traits
        public T addTraits(AgentTrait<?>... genes) {
            this.traits.addAll(asList(checkNotNull(genes)));
            return self();
        }

        public T addTraits(Iterable<? extends AgentTrait<?>> traits) {
            Iterables.addAll(this.traits, checkNotNull(traits));
            return self();
        }

        public T addActions(AgentAction... actions) {
            this.actions.addAll(asList(checkNotNull(actions)));
            return self();
        }

        public T addActions(Iterable<? extends AgentAction> actions) {
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

        private static AgentInitializationFactory createDefaultInitializationFactory() {
            return new AgentInitializationFactory() {
                @Override
                public ActionExecutionStrategy createStrategy(List<? extends AgentAction> actions) {
                    return new DefaultActionExecutionStrategy(actions);
                }

                @Override
                public AgentMessageBox createMessageBox() {
                    return new FixedSizeMessageBox();
                }
            };
        }
    }
}
