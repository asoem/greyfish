package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.genes.GeneComponentList;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.AbstractBuilder;
import org.asoem.greyfish.utils.base.DeepCloner;
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
    protected final ComponentList<GFProperty<?>> properties;

    @Element(name = "actions")
    protected final ComponentList<GFAction> actions;

    @Element(name = "geneComponentList")
    protected final GeneComponentList<GeneComponent<?>> geneComponentList;

    @Element(name = "body")
    protected final Body body;

    @Element(name = "population")
    protected Population population;

    @Element(name = "simulationContext", required = false)
    protected SimulationContext simulationContext = PassiveSimulationContext.INSTANCE;

    @Element(name = "projection", required = false)
    @Nullable
    private MotionObject2D projection;

    @Element(name = "motion", required = false)
    private Motion2D motion = ImmutableMotion2D.noMotion();

    private final AgentMessageBox inBox = new AgentMessageBox();

    protected AbstractAgent(Body body,
                            ComponentList<GFProperty<?>> properties,
                            ComponentList<GFAction> actions,
                            GeneComponentList<GeneComponent<?>> geneComponentList) {
        this.body = checkNotNull(body);
        this.properties = checkNotNull(properties);
        this.actions = checkNotNull(actions);
        this.geneComponentList = checkNotNull(geneComponentList);

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
        cloner.addClone(this);
        this.population = abstractAgent.population;
        this.actions = (ComponentList<GFAction>) cloner.getClone(abstractAgent.actions, ComponentList.class);
        this.properties = (ComponentList<GFProperty<?>>) cloner.getClone(abstractAgent.properties, ComponentList.class);
        this.geneComponentList = cloner.getClone(abstractAgent.geneComponentList, GeneComponentList.class);
        this.body = cloner.getClone(abstractAgent.body, Body.class);
        this.projection = abstractAgent.projection;
        this.motion = abstractAgent.motion;
    }

    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(Population population) {
        this.population = population;
    }

    @Override
    public boolean hasPopulation(Population population) {
        return this.population.equals(population);
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
    public boolean addAction(GFAction action) {
        return addComponent(actions, action);
    }

    @Override
    public boolean removeAction(GFAction action) {
        return removeComponent(actions, action);
    }

    @Override
    public void removeAllActions() {
        clearComponentList(actions);
    }

    @Override
    public <T extends GFAction> T getAction(String name, Class<T> clazz) {
        return actions.find(name, clazz);
    }

    @Override
    public boolean addProperty(GFProperty property) {
        return addComponent(properties, property);
    }

    @Override
    public boolean removeProperty(GFProperty property) {
        return removeComponent(properties, property);
    }

    @Override
    public void removeAllProperties() {
        clearComponentList(properties);
    }

    @Override
    public <T extends GFProperty> T getProperty(String name, Class<T> clazz) {
        return properties.find(name, clazz);
    }

    @Override
    public GFProperty<?> findProperty(Predicate<? super GFProperty<?>> predicate) {
        return properties.find(predicate);
    }

    @Override
    public boolean addGene(GeneComponent<?> gene) {
        return addComponent(geneComponentList, gene);
    }

    @Override
    public boolean removeGene(GeneComponent<?> gene) {
        return removeComponent(geneComponentList, gene);
    }

    @Override
    public void removeAllGenes() {
        clearComponentList(geneComponentList);
    }

    @Override
    public GeneComponentList<GeneComponent<?>> getGeneComponentList() {
        return geneComponentList;
    }

    @Override
    @Nullable
    public <T extends GeneComponent> T getGene(String name, Class<T> clazz) {
        checkNotNull(clazz);

        return geneComponentList.find(name, clazz);
    }

    /**
     * WARNING: This implementation just checks if {@code object} is an {@link Agent} and shares the same population with this agent.
     * It does not check if they are or have been derived from the same prototype via {@link #deepClone}.
     */
    @Override
    public boolean isCloneOf(Object object) {
        // TODO: should be implemented differently. See JavaDoc.
        return object != null
                && Agent.class.isInstance(object) && population.equals(Agent.class.cast(object).getPopulation());

    }

    @Override
    public void changeActionExecutionOrder(GFAction object, GFAction object2) {
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
        return simulationContext.getId();
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
    public Color getColor() {
        return population.getColor();
    }

    @Override
    public void setColor(Color color) {
    }

    @Override
    public GFAction getLastExecutedAction() {
        return simulationContext.getLastExecutedAction();
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
        //checkState(isActive(), "Agents can only log events in an active simulation context");
        //checkState(projection != null, "The Agent must have a projection present");
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        simulationContext.logEvent(this, eventOrigin, title, message);
    }

    @Override
    public void execute() {
        simulationContext.execute(this);
    }

    @Override
    public void shutDown() {
        inBox.clear();
        simulationContext = PassiveSimulationContext.instance();
    }

    @Override
    public boolean isActive() {
        return simulationContext != PassiveSimulationContext.instance();
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
    public void activate(Simulation simulation) {
        simulationContext = new ActiveSimulationContext(simulation);
        logEvent(this, "activated", "");
    }

    @Override
    public void initialize() {
        for (AgentNode node : children()) {
            node.initialize();
        }
    }

    @Override
    public ComponentList<GFProperty<?>> getProperties() {
        return properties;
    }

    @Override
    public ComponentList<GFAction> getActions() {
        return actions;
    }

    @Override
    public Body getBody() {
        return body;
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
    public GeneComponent<?> findTrait(Predicate<? super GeneComponent<?>> traitPredicate) {
        return geneComponentList.find(traitPredicate);
    }

    @Override
    public void setMotion(Motion2D motion) {
        this.motion = checkNotNull(motion);
    }

    @Override
    public void updateGeneComponents(Chromosome chromosome) {
        checkNotNull(chromosome);
        geneComponentList.updateGenes(ImmutableList.copyOf(Iterables.transform(chromosome.getGenes(), new Function<Gene<?>, Object>() {
            @Override
            public Object apply(@Nullable Gene<?> o) {
                assert o != null;
                return o.getAllele();
            }
        })));
        geneComponentList.setOrigin(chromosome.getHistory());
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractAgent that = (AbstractAgent) o;

        if (simulationContext != null ? !simulationContext.equals(that.simulationContext) : that.simulationContext != null)
            return false;
        if (population != null ? !population.equals(that.population) : that.population != null) return false;
        if (projection != null ? !projection.equals(that.projection) : that.projection != null) return false;
        if (actions != null ? !actions.equals(that.actions) : that.actions != null) return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (geneComponentList != null ? !geneComponentList.equals(that.geneComponentList) : that.geneComponentList != null)
            return false;
        if (inBox != null ? !inBox.equals(that.inBox) : that.inBox != null) return false;
        if (motion != null ? !motion.equals(that.motion) : that.motion != null) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = properties != null ? properties.hashCode() : 0;
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        result = 31 * result + (geneComponentList != null ? geneComponentList.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (population != null ? population.hashCode() : 0);
        result = 31 * result + (simulationContext != null ? simulationContext.hashCode() : 0);
        result = 31 * result + (inBox != null ? inBox.hashCode() : 0);
        result = 31 * result + (motion != null ? motion.hashCode() : 0);
        return result;
    }

    protected static abstract class AbstractAgentBuilder<E extends AbstractAgent, T extends AbstractAgentBuilder<E, T>> extends AbstractBuilder<E, T> {
        protected final ComponentList<GFAction> actions = new MutableComponentList<GFAction>();
        protected final ComponentList<GFProperty<?>> properties = new MutableComponentList<GFProperty<?>>();
        protected final Population population;
        protected final ComponentList<GeneComponent<?>> genes = new MutableComponentList<GeneComponent<?>>();

        protected AbstractAgentBuilder(Population population) {
            this.population = checkNotNull(population, "Population must not be null");
        }

        // todo: these builder methods are not able to control the mutability of the genes
        public T addGenes(GeneComponent<?>... genes) {
            this.genes.addAll(asList(checkNotNull(genes)));
            return self();
        }

        public T addActions(GFAction... actions) {
            this.actions.addAll(asList(checkNotNull(actions)));
            return self();
        }

        public T addProperties(GFProperty<?>... properties) {
            this.properties.addAll(asList(checkNotNull(properties)));
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            final Iterable<String> nameWithPossibleDuplicates = Iterables.transform(Iterables.concat(actions, properties, genes), new Function<AgentComponent, String>() {
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
        return "Agent[" + population + ']' + "#" + simulationContext.getId() + "@" + simulationContext.getSimulationStep();
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Iterables.concat(
                Collections.singleton(getBody()),
                getProperties(),
                getActions(),
                getGeneComponentList()
        );
    }
}
